package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileData;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
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
import io.github.loncra.basic.service.ai.server.resolver.state.RpcJsonFileAgentStateStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentSocketSessionResolver;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.service.SnowflakeIdGenerateService;
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
import org.apache.commons.lang3.math.NumberUtils;
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

    private final SnowflakeIdGenerateService snowflakeIdGenerateService;

    @Override
    public ExecutionChannelEnum channel() {
        return ExecutionChannelEnum.REDIS;
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

        ModelSettingMetadata modelMetadata = resolveModelMetadata(body.getModelId());

        AgentConversationEntity conversation = new AgentConversationEntity();
        conversation.setId(Objects.nonNull(body.getAgentConversationId())
                ? body.getAgentConversationId()
                : NumberUtils.toLong(snowflakeIdGenerateService.generateSnowflakeId()));
        conversation.setType(AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
        conversation.setPrincipal(token.getName());
        prepareConversation(conversation, body, modelMetadata);

        AgentMessageEntity userMessage = buildUserMessage(body, modelMetadata, conversation.getId(), token.getName());
        userMessage.setId(NumberUtils.toLong(snowflakeIdGenerateService.generateSnowflakeId()));

        AgentMessageEntity assistantMessage = buildAssistantMessage(userMessage, conversation.getId());
        assistantMessage.setId(NumberUtils.toLong(snowflakeIdGenerateService.generateSnowflakeId()));

        saveConversation(conversation, deviceIdentified);
        saveMessage(userMessage, deviceIdentified);
        saveMessage(assistantMessage, deviceIdentified);

        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(ExecutionChannelEnum.REDIS, assistantMessage.getId().toString(), deviceIdentified),
                false
        );

        AgentChatResponseBody responseBody = buildResponseBody(conversation, userMessage, assistantMessage);

        agentSseStreamPublishResolver.publishStreamStart(conversation.getId().toString(), assistantMessage.getId().toString());

        return responseBody;
    }

    @Override
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentConversationEntity conversation = Objects.requireNonNull(
                getConversation(NumberUtils.toLong(body.getClientConversationId()), deviceIdentified),
                "本地执行会话不存在"
        );
        AgentMessageEntity assistantMessage = Objects.requireNonNull(
                getMessage(body.getAssistantMessageId(), deviceIdentified),
                "本地执行数据不存在或已清理，无法恢复确认"
        );
        AgentMessageEntity userMessage = Objects.requireNonNull(
                getMessage(assistantMessage.getParentId(), deviceIdentified),
                "找不到 ID 为 [" + assistantMessage.getParentId() + "] 的用户消息记录"
        );
        PrincipalDetailsConstants.equals(assistantMessage, token);
        SystemException.isTrue(
                Objects.equals(String.valueOf(assistantMessage.getAgentConversationId()), body.getClientConversationId()),
                "助手消息与会话不一致"
        );
        SystemException.isTrue(
                AgentChatStatusEnum.REQUEST_STOP.equals(assistantMessage.getStatus()),
                "当前助手消息不在等待确认状态"
        );

        List<ConfirmResult> confirmResults = getResumeConfirmResults(body, token, deviceIdentified);
        assistantMessage.saveUserConfirmResultMetadata(confirmResults);
        applyConfirmResults(assistantMessage, confirmResults);

        saveConversation(conversation, deviceIdentified);
        saveMessage(userMessage, deviceIdentified);
        saveMessage(assistantMessage, deviceIdentified);

        agentSseStreamPublishResolver.publishStreamStart(assistantMessage.getAgentConversationId().toString(), assistantMessage.getId().toString());
        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(ExecutionChannelEnum.REDIS, assistantMessage.getId().toString(), deviceIdentified),
                false
        );

        AgentChatBasicResponseBody result = new AgentChatBasicResponseBody();
        result.setAssistantMessageId(assistantMessage.getId());
        result.setUserMessageId(assistantMessage.getParentId());

        return result;
    }

    @Override
    public AgentExecutionMetadata getAgentExecutionMetadata(AgentExecuteMessageMetadata message) {
        String deviceIdentified = message.getDeviceIdentified();
        AgentMessageEntity assistant = getMessage(NumberUtils.toLong(message.getRef()), deviceIdentified);
        if (Objects.isNull(assistant)) {
            return null;
        }

        AgentMessageEntity userMessage = Objects.requireNonNull(
                getMessage(assistant.getParentId(), deviceIdentified),
                "找不到 ID 为 [" + assistant.getParentId() + "] 的用户消息记录"
        );
        AgentConversationEntity conversation = Objects.requireNonNull(
                getConversation(assistant.getAgentConversationId(), deviceIdentified),
                "找不到 ID 为 [" + assistant.getAgentConversationId() + "] 的会话内容"
        );

        AgentExecutionMetadata execution = new AgentExecutionMetadata();
        execution.setChannel(ExecutionChannelEnum.REDIS);
        execution.setDeviceIdentified(deviceIdentified);
        execution.setClientConversationId(assistant.getAgentConversationId().toString());
        execution.setConversation(conversation);
        execution.setUserMessage(userMessage);
        execution.setAssistantMessage(assistant);
        return execution;
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
    public AbstractFilesystem createFilesystem(AgentExecutionMetadata execution, String workspacePath) {
        return new RemoteRpcFilesystem(rpcFilesystemInvoker, execution.getDeviceIdentified());
    }

    @Override
    protected AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution) {
        return new RpcJsonFileAgentStateStore(rpcFilesystemInvoker, execution.getDeviceIdentified());
    }

    @Override
    public void saveConversation(AgentConversationEntity conversation, String deviceIdentified) {
        writeString(deviceIdentified, conversationFile(conversation.getId()), toJson(conversation));
    }

    @Override
    public AgentConversationEntity getConversation(Long conversationId, String deviceIdentified) {
        return fromJson(readString(deviceIdentified, conversationFile(conversationId)), AgentConversationEntity.class);
    }

    @Override
    public void saveMessage(AgentMessageEntity message, String deviceIdentified) {
        writeString(deviceIdentified, messageFile(message.getId()), toJson(message));
        List<Long> messageIds = readMessageIds(message.getAgentConversationId(), deviceIdentified);
        if (!messageIds.contains(message.getId())) {
            messageIds.add(message.getId());
            writeString(deviceIdentified, messageIndexFile(message.getAgentConversationId()), toJson(messageIds));
        }
    }

    @Override
    public AgentMessageEntity getMessage(Long messageId, String deviceIdentified) {
        return fromJson(readString(deviceIdentified, messageFile(messageId)), AgentMessageEntity.class);
    }

    @Override
    public List<AgentMessageEntity> getConversationMessages(Long conversationId, String deviceIdentified) {
        return readMessageIds(conversationId, deviceIdentified).stream()
                .map(id -> getMessage(id, deviceIdentified))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AgentMessageEntity> findRequestStopAssistantMessage(Long conversationId, String deviceIdentified) {
        return getConversationMessages(conversationId, deviceIdentified).stream()
                .filter(s -> AgentChatStatusEnum.REQUEST_STOP.equals(s.getStatus()))
                .filter(s -> AgentMessageRoleEnum.ASSISTANT.equals(s.getRole()))
                .toList();
    }

    @Override
    public void deleteConversationData(Long conversationId, String deviceIdentified) {
        AgentConversationEntity conversation = getConversation(conversationId, deviceIdentified);
        if (Objects.nonNull(conversation)) {
            BasicAgentExecutionMetadata basic = new BasicAgentExecutionMetadata();
            basic.setChannel(ExecutionChannelEnum.REDIS);
            basic.setDeviceIdentified(deviceIdentified);
            basic.setClientConversationId(conversationId.toString());
            resolveStateStore(basic).delete(AgentMessageEntity.obtainUserId(conversation.getPrincipal()), conversationId.toString());
        }
        for (Long messageId : readMessageIds(conversationId, deviceIdentified)) {
            deleteFile(deviceIdentified, messageFile(messageId));
        }
        deleteFile(deviceIdentified, messageIndexFile(conversationId));
        deleteFile(deviceIdentified, conversationFile(conversationId));
    }

    @Override
    public void deleteConversation(List<Integer> ids, AuditAuthenticationToken token, String deviceIdentified) {
        for (Integer id : ids) {
            AgentConversationEntity conversation = Objects.requireNonNull(
                    getConversation(id.longValue(), deviceIdentified),
                    "找不到 ID 为 [" + id + "] 的会话内容"
            );
            PrincipalDetailsConstants.equals(conversation, token);
            SystemException.isTrue(!AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(conversation.getType()), "无法删除 [" + conversation.getType().getName() + "]类型的空间");
            deleteConversationData(conversation.getId(), deviceIdentified);
            agentSseStreamPublishResolver.remove(conversation.getId().toString());
        }
    }

    @Override
    public Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            Long conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentConversationEntity conversation = Objects.requireNonNull(
                getConversation(conversationId, deviceIdentified),
                "找不到 ID 为 [" + conversationId + "] 的会话内容"
        );
        PrincipalDetailsConstants.equals(conversation, token);
        List<AgentMessageEntity> messages = getConversationMessages(conversationId, deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentMessageEntity::getId).reversed())
                .toList();
        List<AgentMessageEntity> elements = messages.subList(
                Math.min(request.getOffset(), messages.size()),
                Math.min(request.getOffset() + request.getSize(), messages.size())
        );
        return totalPage ? new TotalPage<>(request, elements, messages.size()) : new Page<>(request, elements);
    }

    @Override
    public int positioningMessagePageNumber(Long conversationId, Long messageId, int pageSize, String deviceIdentified) {
        List<AgentMessageEntity> messages = getConversationMessages(conversationId, deviceIdentified).stream()
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
    public void saveAssistant(AgentExecutionMetadata execution, AgentMessageEntity assistant) {
        saveMessage(assistant, execution.getDeviceIdentified());
    }

    @Override
    public void saveConversationStatus(AgentExecutionMetadata execution, AgentChatStatusEnum status) {
        AgentConversationEntity conversation = execution.getConversation();
        if (Objects.isNull(conversation)) {
            return;
        }
        conversation.setStatus(status);
        saveConversation(conversation, execution.getDeviceIdentified());
    }

    @Override
    protected String createEnvironmentMemory(String deviceIdentified, String principal) {

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

    private String conversationFile(Long conversationId) {
        return STORE_ROOT + "/" + CONVERSATIONS_DIR_NAME + "/" + conversationId + ".json";
    }

    private String messageFile(Long messageId) {
        return STORE_ROOT + "/" + MESSAGES_DIR_NAME + "/" + messageId + ".json";
    }

    private String messageIndexFile(Long conversationId) {
        return STORE_ROOT + "/" + INDEXES_DIR_NAME + "/" + conversationId + ".json";
    }

    private List<Long> readMessageIds(Long conversationId, String deviceIdentified) {
        String json = readString(deviceIdentified, messageIndexFile(conversationId));
        if (Objects.isNull(json)) {
            return new LinkedList<>();
        }
        List<?> raw = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, List.class));
        return raw.stream()
                .map(n -> ((Number) n).longValue())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private String toJson(Object value) {
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(value));
    }

    private <T> T fromJson(String json, Class<T> type) {
        if (Objects.isNull(json)) {
            return null;
        }
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, type));
    }

    private void writeString(String deviceIdentified, String path, String content) {
        RestResult<Object> result = invoke(
                deviceIdentified,
                RpcFilesystemOperationEnum.WRITE,
                Map.of(PATH_FIELD_NAME, path, CONTENT_FIELD_NAME, content)
        );
        SystemException.isTrue(result.isSuccess(), "写入客户端执行数据文件 [" + path + "] 失败: " + result.getMessage());
    }

    private String readString(String deviceIdentified, String path) {
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

    private void deleteFile(String deviceIdentified, String path) {
        invoke(deviceIdentified, RpcFilesystemOperationEnum.DELETE, Map.of(PATH_FIELD_NAME, path));
    }

    private RestResult<Object> invoke(
            String deviceIdentified,
            RpcFilesystemOperationEnum operation,
            Map<String, Object> params
    ) {
        return rpcFilesystemInvoker.invoke(deviceIdentified, operation, params);
    }
}
