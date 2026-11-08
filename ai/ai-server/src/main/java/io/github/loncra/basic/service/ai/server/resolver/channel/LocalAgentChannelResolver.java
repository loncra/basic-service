package io.github.loncra.basic.service.ai.server.resolver.channel;

import com.fasterxml.jackson.core.type.TypeReference;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileData;
import io.agentscope.harness.agent.filesystem.model.FileInfo;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.RpcFilesystemOperationEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RemoteRpcFilesystem;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemScope;
import io.github.loncra.basic.service.ai.server.resolver.state.RpcJsonFileAgentStateStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentSocketSessionResolver;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.api.domian.metadata.SocketSessionMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.TotalPage;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocalAgentChannelResolver extends AbstractAgentChannelResolver {

    private static final String STORE_ROOT = "/workspace/.loncra-agent";

    private static final String CONVERSATIONS_DIR_NAME = "conversations";

    private static final String MESSAGES_DIR_NAME = "messages";

    private static final String INDEXES_DIR_NAME = "indexes";

    private static final String PATH_FIELD_NAME = "path";

    private static final String OFFSET_FIELD_NAME = "offset";

    private static final String LIMIT_FIELD_NAME = "limit";

    private static final String CONTENT_FIELD_NAME = "content";

    private final AgentSocketSessionResolver agentSocketSessionResolver;

    private final RpcFilesystemInvoker rpcFilesystemInvoker;

    @Override
    public ExecutionChannelEnum getChannel() {
        return ExecutionChannelEnum.LOCAL;
    }

    @Override
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        SocketSessionMetadata session = agentSocketSessionResolver.getSession(token.getName(), deviceIdentified);
        SystemException.isTrue(Objects.nonNull(session), "本地工作空间执行需要桌面客户端在线");
        String executeShell = Objects.toString(session.getMetadata().get(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME), null);
        SystemException.isTrue(StringUtils.isNotBlank(executeShell), "客户端 [" + deviceIdentified + "] 未上报 executeShell，无法执行本地任务");

        return super.chat(body,token, deviceIdentified);
    }

    @Override
    protected AgentConversationEntity getDefaultWorkspace(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return listConversationEntities(token.getName(), deviceIdentified).stream()
                .filter(s -> AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(s.getType()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentChatBasicResponseBody responseBody = super.resume(body, token, deviceIdentified);
        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(getChannel(), responseBody.getAssistantMessageId(), deviceIdentified),
                false
        );
        return responseBody;
    }

    @Override
    protected List<AgentConversationEntity> doFindConversations(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return listConversationEntities(token.getName(), deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentConversationEntity::getId).reversed())
                .toList();
    }

    @Override
    public HarnessAgent createHarnessAgent(AgentExecutionMetadata execution) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        AgentMessageEntity userMessage = Objects.requireNonNull(
                execution.getUserMessage(),
                "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录"
        );

        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(
                assistant.getModel(),
                userMessage.getMetadata()
        );

        String workspacePath = aiAppConfig.getWorkspacePath() + File.separator + execution.getClientConversationId();

        return finishAgent(baseBuilder(execution, modelResolverMetadata, workspacePath), execution);
    }

    @Override
    public AbstractFilesystem createFilesystem(
            AgentExecutionMetadata execution,
            String workspacePath
    ) {
        String principal = execution.getAssistantMessage().getPrincipal();
        AgentConversationEntity conversation = execution.getConversation();
        RpcFilesystemScope workspaceScope = Objects.isNull(conversation)
                ? new RpcFilesystemScope.DefaultWorkspace()
                : resolveWorkspaceScope(conversation, principal, execution.getDeviceIdentified());
        return new RemoteRpcFilesystem(rpcFilesystemInvoker, execution.getDeviceIdentified(), principal, workspaceScope);
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return new RpcJsonFileAgentStateStore(rpcFilesystemInvoker, execution);
    }

    @Override
    public void updateConversation(
            AgentConversationEntity conversation,
            String deviceIdentified
    ) {
        writeString(deviceIdentified, conversationFile(conversation.getPrincipal(), conversation.getId()), toJson(conversation));
    }

    @Override
    public void insertConversation(
            AgentConversationEntity conversation,
            String deviceIdentified
    ) {
        writeString(deviceIdentified, conversationFile(conversation.getPrincipal(), conversation.getId()), toJson(conversation));
    }

    @Override
    public AgentConversationEntity getConversation(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        return fromJson(readString(deviceIdentified, conversationFile(principal, conversationId)), AgentConversationEntity.class);
    }

    @Override
    public void saveMessage(
            AgentMessageEntity message,
            String deviceIdentified
    ) {
        String principal = message.getPrincipal();
        writeString(deviceIdentified, messageFile(principal, message.getId()), toJson(message));
        List<String> messageIds = readMessageIds(principal, message.getAgentConversationId(), deviceIdentified);
        if (!messageIds.contains(message.getId())) {
            messageIds.add(message.getId());
            writeString(deviceIdentified, messageIndexFile(principal, message.getAgentConversationId()), toJson(messageIds));
        }
    }

    @Override
    public AgentMessageEntity getMessage(
            String principal,
            String messageId,
            String deviceIdentified
    ) {
        return fromJson(readString(deviceIdentified, messageFile(principal, messageId)), AgentMessageEntity.class);
    }

    @Override
    public List<AgentMessageEntity> getConversationMessages(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        return readMessageIds(principal, conversationId, deviceIdentified).stream()
                .map(id -> getMessage(principal, id, deviceIdentified))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AgentMessageEntity> findRequestStopAssistantMessage(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        return getConversationMessages(principal, conversationId, deviceIdentified).stream()
                .filter(s -> AgentChatStatusEnum.REQUEST_STOP.equals(s.getStatus()))
                .filter(s -> AgentMessageRoleEnum.ASSISTANT.equals(s.getRole()))
                .toList();
    }

    @Override
    protected void doDeleteConversation(
            AgentConversationEntity conversation,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        String principal = conversation.getPrincipal();
        for (String messageId : readMessageIds(principal, conversation.getId(), deviceIdentified)) {
            deleteFile(deviceIdentified, messageFile(principal, messageId));
        }
        deleteFile(deviceIdentified, messageIndexFile(principal, conversation.getId()));
        deleteFile(deviceIdentified, conversationFile(principal, conversation.getId()));
        List<AgentConversationEntity> entities = listConversationEntities(principal, deviceIdentified);
        entities.stream()
                .filter(s -> Strings.CS.equals(s.getParentId(), conversation.getId()))
                .forEach(s -> deleteConversation(s.getId(), token, deviceIdentified));
    }

    private List<AgentConversationEntity> listConversationEntities(String principal, String deviceIdentified) {
        RestResult<Object> result = rpcFilesystemInvoker.invoke(
                deviceIdentified,
                new RpcFilesystemScope.Store(),
                RpcFilesystemOperationEnum.LS,
                Map.of(PATH_FIELD_NAME, storeRoot(principal) + "/" + CONVERSATIONS_DIR_NAME)
        );
        if (!result.isSuccess() || Objects.isNull(result.getData())) {
            return List.of();
        }
        List<FileInfo> files = CastUtils.convertValue( result.getData(), new TypeReference<>() {});
        return files.stream()
                .map(FileInfo::path)
                .filter(Objects::nonNull)
                .map(path -> path.substring(path.lastIndexOf('/') + 1))
                .filter(name -> name.endsWith(".json"))
                .map(name -> name.substring(0, name.length() - ".json".length()))
                .map(id -> getConversation(principal, id, deviceIdentified))
                .filter(Objects::nonNull)
                .toList();
    }

    private RpcFilesystemScope resolveWorkspaceScope(
            AgentConversationEntity conversation,
            String principal,
            String deviceIdentified
    ) {
        if (AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(conversation.getType())) {
            return new RpcFilesystemScope.DefaultWorkspace();
        }
        String key = Objects.toString(conversation.getMetadata().get(WORKSPACE_KEY_METADATA_KEY), null);
        if (AgentConversationTypeEnum.CUSTOMIZE_WORKSPACE.equals(conversation.getType()) && StringUtils.isNotBlank(key)) {
            return new RpcFilesystemScope.CustomWorkspace(key);
        }
        AgentConversationEntity parent = Objects.isNull(conversation.getParentId())
                ? null
                : getConversation(principal, conversation.getParentId(), deviceIdentified);
        if (Objects.isNull(parent)) {
            return new RpcFilesystemScope.DefaultWorkspace();
        }
        return resolveWorkspaceScope(parent, principal, deviceIdentified);
    }

    @Override
    public Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            String conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        String principal = token.getName();
        AgentConversationEntity conversation = Objects.requireNonNull(
                getConversation(principal, conversationId, deviceIdentified),
                "找不到 ID 为 [" + conversationId + "] 的会话内容"
        );
        PrincipalDetailsConstants.equals(conversation, token);
        List<AgentMessageEntity> messages = getConversationMessages(principal, conversationId, deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentMessageEntity::getId).reversed())
                .toList();
        List<AgentMessageEntity> elements = messages.subList(
                Math.min(request.getOffset(), messages.size()),
                Math.min(request.getOffset() + request.getSize(), messages.size())
        );
        return totalPage ? new TotalPage<>(request, elements, messages.size()) : new Page<>(request, elements);
    }

    @Override
    public int positioningMessagePageNumber(
            String principal,
            String conversationId,
            String messageId,
            int pageSize,
            String deviceIdentified
    ) {
        List<AgentMessageEntity> messages = getConversationMessages(principal, conversationId, deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentMessageEntity::getId).reversed())
                .toList();
        int index = 0;
        for (AgentMessageEntity message : messages) {
            if (messageId.equals(message.getId())) {
                break;
            }
            index++;
        }
        return index >= messages.size() ? 1 : index / pageSize + 1;
    }

    @Override
    public void saveAssistant(
            AgentExecutionMetadata execution,
            AgentMessageEntity assistant
    ) {
        saveMessage(assistant, execution.getDeviceIdentified());
    }

    @Override
    public void saveConversationStatus(
            AgentExecutionMetadata execution,
            AgentChatStatusEnum status
    ) {
        AgentConversationEntity conversation = execution.getConversation();
        if (Objects.isNull(conversation)) {
            return;
        }
        conversation.setStatus(status);
        updateConversation(conversation, execution.getDeviceIdentified());
    }

    @Override
    protected String createEnvironmentMemory(
            String deviceIdentified,
            String principal
    ) {

        SocketSessionMetadata session = agentSocketSessionResolver.getSession(principal, deviceIdentified);

        if (Objects.isNull(session)) {
            return null;
        }

        String userAgentValue = Objects.toString(session.getMetadata().get(DeviceUtils.USER_AGENT_HEADER_NAME.toLowerCase()), null);
        if (StringUtils.isBlank(userAgentValue)) {
            return null;
        }

        UserAgent userAgent = DeviceUtils.getDevice(userAgentValue);
        String operatingSystemName = userAgent.get(UserAgent.OPERATING_SYSTEM_NAME).getValue();
        String operatingSystemClass = userAgent.get(UserAgent.OPERATING_SYSTEM_CLASS).getValue();

        if (StringUtils.isBlank(operatingSystemName)) {
            return null;
        }

        String executeShell = Objects.toString(session.getMetadata().get(SocketSessionMetadata.EXECUTE_SHELL_METADATA_NAME), null);
        if (StringUtils.isBlank(executeShell)) {
            return null;
        }

        return MessageFormat.format(
                "客户端环境：操作系统 {0}（{1}），默认 shell 为 {2}，文件系统根路径为 /workspace，所有文件路径以 /workspace 为根，执行命令时请使用与 {2} 匹配的语法。",
                operatingSystemName,
                operatingSystemClass,
                executeShell
        );
    }

    private String storeRoot(String principal) {
        return STORE_ROOT + "/" + segment(principal);
    }

    private String segment(String value) {
        return value.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }

    private String conversationFile(String principal, String conversationId) {
        return storeRoot(principal) + "/" + CONVERSATIONS_DIR_NAME + "/" + conversationId + ".json";
    }

    private String messageFile(String principal, String messageId) {
        return storeRoot(principal) + "/" + MESSAGES_DIR_NAME + "/" + messageId + ".json";
    }

    private String messageIndexFile(String principal, String conversationId) {
        return storeRoot(principal) + "/" + INDEXES_DIR_NAME + "/" + conversationId + ".json";
    }

    private List<String> readMessageIds(
            String principal,
            String conversationId,
            String deviceIdentified
    ) {
        String json = readString(deviceIdentified, messageIndexFile(principal, conversationId));
        if (Objects.isNull(json)) {
            return new LinkedList<>();
        }
        List<?> raw = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, List.class));
        return raw.stream()
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private String toJson(Object value) {
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(value));
    }

    private <T> T fromJson(
            String json,
            Class<T> type
    ) {
        if (Objects.isNull(json)) {
            return null;
        }
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, type));
    }

    private void writeString(
            String deviceIdentified,
            String path,
            String content
    ) {
        RestResult<Object> result = invoke(
                deviceIdentified,
                RpcFilesystemOperationEnum.WRITE,
                Map.of(PATH_FIELD_NAME, path, CONTENT_FIELD_NAME, content)
        );
        SystemException.isTrue(result.isSuccess(), "写入客户端执行数据文件 [" + path + "] 失败: " + result.getMessage());
    }

    private String readString(
            String deviceIdentified,
            String path
    ) {
        RestResult<Object> result = invoke(
                deviceIdentified,
                RpcFilesystemOperationEnum.READ,
                Map.of(PATH_FIELD_NAME, path, OFFSET_FIELD_NAME, 0, LIMIT_FIELD_NAME, 0)
        );
        if (!result.isSuccess()) {
            return null;
        }
        FileData fileData = CastUtils.convertValue(result.getData(), FileData.class);
        return Objects.isNull(fileData) ? null : fileData.content();
    }

    private void deleteFile(
            String deviceIdentified,
            String path
    ) {
        invoke(deviceIdentified, RpcFilesystemOperationEnum.DELETE, Map.of(PATH_FIELD_NAME, path));
    }

    private RestResult<Object> invoke(
            String deviceIdentified,
            RpcFilesystemOperationEnum operation,
            Map<String, Object> params
    ) {
        return rpcFilesystemInvoker.invoke(deviceIdentified, new RpcFilesystemScope.Store(), operation, params);
    }
}
