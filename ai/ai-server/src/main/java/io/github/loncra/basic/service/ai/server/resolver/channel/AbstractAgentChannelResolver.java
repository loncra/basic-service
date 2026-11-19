package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.ConversationConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentRecordStore;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.FilesystemAgentRecordStore;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.FilesystemAgentStateStore;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.SourceOfTruthFileSystemFactory;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.ai.server.service.agent.UserPluginRuntimeContextSkillRepository;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.service.SnowflakeIdGenerateService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Function;

@Setter(onMethod_ = {@Autowired})
public abstract class AbstractAgentChannelResolver implements AgentChannelResolver {

    protected ModelSettingService modelSettingService;

    protected AiAppConfig aiAppConfig;

    protected SourceOfTruthFileSystemFactory sourceOfTruthFileSystemFactory;

    protected List<MiddlewareBase> middlewares;

    protected AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    protected SnowflakeIdGenerateService snowflakeIdGenerateService;

    protected UserPluginRuntimeContextSkillRepository userPluginRuntimeContextSkillRepository;

    protected ConversationConfig conversationConfig;

    protected ModelSettingMetadata resolveModelMetadata(Long modelId) {
        ModelSettingEntity model = Objects.requireNonNull(modelSettingService.get(modelId), "找不到 ID 为 [" + modelId + "] 的模型数据");
        return CastUtils.of(model, ModelSettingMetadata.class);
    }

    protected AgentMessageEntity buildUserMessage(
            AgentChatRequestBody body,
            ModelSettingMetadata modelMetadata,
            String conversationId,
            String principal
    ) {
        AgentMessageEntity userMessage = CastUtils.of(body, AgentMessageEntity.class);
        userMessage.setId(snowflakeIdGenerateService.generateSnowflakeId());
        userMessage.setModel(modelMetadata);
        userMessage.setRole(AgentMessageRoleEnum.USER);
        userMessage.setPrincipal(principal);
        userMessage.setStatus(AgentChatStatusEnum.READY);
        userMessage.setAgentConversationId(conversationId);
        return userMessage;
    }

    protected AgentMessageEntity buildAssistantMessage(
            AgentMessageEntity userMessage,
            String conversationId
    ) {
        AgentMessageEntity assistantMessage = CastUtils.of(userMessage, AgentMessageEntity.class, IdEntity.ID_FIELD_NAME);
        assistantMessage.setId(snowflakeIdGenerateService.generateSnowflakeId());
        assistantMessage.setRole(AgentMessageRoleEnum.ASSISTANT);
        assistantMessage.setParentId(userMessage.getId());
        assistantMessage.setContent(new LinkedList<>());
        assistantMessage.setAgentConversationId(conversationId);
        return assistantMessage;
    }

    protected AgentChatResponseBody buildResponseBody(
            AgentConversationEntity conversation,
            AgentMessageEntity userMessage,
            AgentMessageEntity assistantMessage
    ) {
        AgentChatResponseBody responseBody = new AgentChatResponseBody();
        responseBody.setConversation(conversation);
        responseBody.setUserMessageId(userMessage.getId());
        responseBody.setAssistantMessageId(assistantMessage.getId());
        return responseBody;
    }

    protected AgentConversationEntity buildBasicConversation(
            String principal,
            AgentConversationTypeEnum type
    ) {
        AgentConversationEntity entity = new AgentConversationEntity();
        entity.setId(snowflakeIdGenerateService.generateSnowflakeId());
        entity.setStatus(AgentChatStatusEnum.READY);
        entity.setPrincipal(principal);
        entity.setType(type);
        if (type.equals(AgentConversationTypeEnum.WORKSPACE_CONVERSATION)) {
            entity.setGenerateNameStatus(ExecuteStatus.Pending);
        } else {
            entity.setGenerateNameStatus(ExecuteStatus.Success);
        }
        entity.getMetadata().put(AgentChannelResolver.WORKSPACE_KIND_METADATA_KEY, getChannel());
        return entity;
    }

    @Override
    public List<AgentConversationEntity> findConversations(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        List<AgentConversationEntity> data = getAgentRecordStore().listConversations(token.getName(), deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentConversationEntity::getId).reversed())
                .toList();
        List<AgentConversationEntity> result = new LinkedList<>(data);
        if (data.stream().noneMatch(s -> AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(s.getType()))) {
            AgentConversationEntity entity = createDefaultIfNotExist(token, deviceIdentified);
            result.add(entity);
        }
        return TreeUtils.buildGenericTree(result);
    }

    protected AgentConversationEntity createDefaultIfNotExist(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentConversationEntity entity = getDefaultWorkspace(token, deviceIdentified);

        if (Objects.isNull(entity)) {
            IdNameMetadata body = IdNameMetadata.of(StringUtils.EMPTY, conversationConfig.getDefaultWorkspaceName());
            entity = saveWorkspace(
                    body,
                    token,
                    deviceIdentified,
                    AgentConversationTypeEnum.DEFAULT_WORKSPACE
            );
        }

        return entity;
    }

    public AgentConversationEntity getDefaultWorkspace(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        return getAgentRecordStore().listConversations(token.getName(), deviceIdentified).stream()
                .filter(item -> AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(item.getType()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public AgentConversationEntity saveWorkspace(
            IdNameMetadata body,
            AuditAuthenticationToken token,
            String deviceIdentified,
            AgentConversationTypeEnum type
    ) {
        AgentConversationEntity entity;
        if (StringUtils.isEmpty(body.getId())) {
            entity = buildBasicConversation(token.getName(), type);
            entity.setName(body.getName());
            entity.getMetadata().putAll(body.getMetadata());

            preCreateWorkspace(entity, token, deviceIdentified);
            getAgentRecordStore().insertConversation(entity, deviceIdentified);
        } else {
            entity = getAgentRecordStore().getConversation(token.getName(), body.getId(), deviceIdentified);
            entity.setName(body.getName());
            entity.getMetadata().putAll(body.getMetadata());

            preCreateWorkspace(entity, token, deviceIdentified);
            getAgentRecordStore().updateConversation(entity, deviceIdentified);
        }

        return entity;
    }

    protected abstract void preCreateWorkspace(
            AgentConversationEntity workspace,
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    @Override
    public HarnessAgent createHarnessAgent(AgentExecutionMetadata execution) {

        ModelResolverMetadata modelResolverMetadata = modelSettingService.getModelMetadata(
                execution.getAssistantMessage().getModel(),
                execution.getUserMessage().getMetadata()
        );

        HarnessAgent.Builder builder = HarnessAgent.builder()
                .name(AiAppConfig.DEFAULT_AGENT_NAME)
                .sysPrompt(aiAppConfig.getSystemPrompt())
                .model(modelResolverMetadata.getModel())
                .toolkit(modelResolverMetadata.getToolkit())
                .enableMetaTool(true)
                .stateStore(getAgentStateStore(execution))
                .compaction(aiAppConfig.toCompactionConfig())
                .transcriptTenant(SourceOfTruthFileSystemFactory.TRANSCRIPTS_DIR)
                .skillRepository(userPluginRuntimeContextSkillRepository)
                .middlewares(middlewares);
        AgentMessageEntity assistant = execution.getAssistantMessage();

        if (!AgentChatTypeEnum.ASK.equals(assistant.getType())) {
            builder.enablePlanMode();
        }

        return definitionHarnessAgent(builder, execution);
    }

    protected abstract HarnessAgent definitionHarnessAgent(
            HarnessAgent.Builder builder,
            AgentExecutionMetadata execution
    );

    protected Map<String, AbstractFilesystem> userLevelRouteMap(Function<String, AbstractFilesystem> storeBackend) {
        Map<String, AbstractFilesystem> routes = new LinkedHashMap<>();
        for (SourceOfTruthFileSystemFactory.UserLevelRoute route : SourceOfTruthFileSystemFactory.USER_LEVEL_ROUTES) {
            routes.put(route.prefix(), storeBackend.apply(route.dir()));
        }
        return routes;
    }

    @Override
    public AgentStateStore getAgentStateStore(BasicAgentExecutionMetadata execution) {
        return new FilesystemAgentStateStore(openRecords(execution.getDeviceIdentified()));
    }

    protected List<ConfirmResult> getResumeConfirmResults(
            List<AgentResumeRequestBody.ConfirmResult> confirmResults,
            BasicAgentExecutionMetadata basic,
            AuditAuthenticationToken token
    ) {
        AgentState state = getAgentStateStore(basic)
                .get(
                        AgentMessageEntity.obtainUserId(token.getName()),
                        basic.getClientConversationId(),
                        FilesystemAgentStateStore.AGENT_STATE_KEY,
                        AgentState.class
                )
                .orElseThrow(() -> new SystemException("找不到会话 [" + basic.getClientConversationId() + "] 的执行状态，无法恢复确认"));

        List<ToolUseBlock> askingToolCalls = findAskingToolCalls(state);
        SystemException.isTrue(!askingToolCalls.isEmpty(), "会话 [" + basic.getClientConversationId() + "] 没有等待确认的工具调用");

        List<ConfirmResult> results = new LinkedList<>();
        for (ToolUseBlock toolUseBlock : askingToolCalls) {
            Boolean confirm = confirmResults
                    .stream()
                    .filter(s -> s.getToolCallId().equals(toolUseBlock.getId()))
                    .findFirst()
                    .map(AgentResumeRequestBody.ConfirmResult::isConfirmed)
                    .orElseThrow(() -> new SystemException("ID 为 [" + toolUseBlock.getId() + "] 的工具需要确认"));
            results.add(new ConfirmResult(confirm, toolUseBlock));
        }
        return results;
    }

    protected static void applyConfirmResults(
            AgentMessageEntity assistantMessage,
            List<ConfirmResult> confirmResults
    ) {
        for (ConfirmResult confirmResult : confirmResults) {
            assistantMessage.obtainMessageContents().stream()
                    .filter(s -> confirmResult.getToolCall().getId().equals(s.getId()))
                    .map(s -> CastUtils.cast(s, ToolCallBlockContentMetadata.class))
                    .findFirst()
                    .ifPresent(contentMetadata -> {
                        contentMetadata.setUserConfirmed(confirmResult.isConfirmed());
                        contentMetadata.setHitlStatus(confirmResult.isConfirmed() ? ToolCallState.ALLOWED : ToolCallState.FINISHED);
                        assistantMessage.updateContent(contentMetadata);
                    });
        }
    }

    private List<ToolUseBlock> findAskingToolCalls(AgentState state) {
        List<Msg> context = state.getContext();
        for (int i = context.size() - 1; i >= 0; i--) {
            Msg msg = context.get(i);
            if (!MsgRole.ASSISTANT.equals(msg.getRole())) {
                continue;
            }
            return msg.getContent().stream()
                    .filter(b -> b instanceof ToolUseBlock t && ToolCallState.ASKING.equals(t.getState()))
                    .map(ToolUseBlock.class::cast)
                    .toList();
        }
        return List.of();
    }

    @Override
    public AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentRecordStore agentRecordStore = getAgentRecordStore();
        AgentConversationEntity conversation;

        if (Objects.isNull(body.getAgentConversationId())) {
            conversation = getDefaultWorkspace(token, deviceIdentified);
            SystemException.isTrue(Objects.nonNull(conversation), "默认工作空间不存在，请先打开智能体页面完成初始化");
        } else {
            conversation = Objects.requireNonNull(
                    agentRecordStore.getConversation(token.getName(), body.getAgentConversationId(), deviceIdentified),
                    "找不到 ID 为 [" + body.getAgentConversationId() + "] 的会话内容"
            );
        }

        ModelSettingMetadata modelMetadata = resolveModelMetadata(body.getModelId());

        if (conversation.getType() != AgentConversationTypeEnum.WORKSPACE_CONVERSATION) {
            String parentId = conversation.getId();
            Map<String,Object> metadata = conversation.getMetadata();
            conversation = buildBasicConversation(token.getName(), AgentConversationTypeEnum.WORKSPACE_CONVERSATION);
            conversation.setParentId(parentId);
            conversation.setMetadata(metadata);
            conversation.setLastModel(modelMetadata);
            conversation.setLastChatType(body.getType());
            conversation.setGenerateNameStatus(conversationConfig.isGenerateName() ? ExecuteStatus.Pending : ExecuteStatus.Success);
            conversation.setName(StringUtils.defaultIfEmpty(conversation.getName(), conversationConfig.getNewConversation()));
            conversation.setStatus(AgentChatStatusEnum.READY);
            getAgentRecordStore().insertConversation(conversation, deviceIdentified);
        } else {
            /* assertNoRunningAssistant(conversation.getId());*/
            //FIXME 这里处理一下排队内容
        }

        AgentMessageEntity userMessage = buildUserMessage(body, modelMetadata, conversation.getId(), token.getName());

        AgentMessageEntity assistantMessage = buildAssistantMessage(userMessage, conversation.getId());
        agentRecordStore.saveMessage(userMessage, deviceIdentified);
        agentRecordStore.saveMessage(assistantMessage, deviceIdentified);

        agentSseStreamPublishResolver.sendExecuteMessage(
                AgentExecuteMessageMetadata.of(getChannel(), assistantMessage.getId(), deviceIdentified),
                false
        );

        AgentChatResponseBody responseBody = buildResponseBody(conversation, userMessage, assistantMessage);

        agentSseStreamPublishResolver.publishStreamStart(conversation.getId(), assistantMessage.getId());

        return responseBody;
    }

    @Override
    public AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentMessageEntity assistantMessage = Objects.requireNonNull(getAgentRecordStore().getMessage(token.getName(), body.getAssistantMessageId(), deviceIdentified));
        PrincipalDetailsConstants.equals(assistantMessage, token);

        BasicAgentExecutionMetadata basic = new BasicAgentExecutionMetadata();
        basic.setChannel(getChannel());
        basic.setClientConversationId(assistantMessage.getAgentConversationId());
        basic.setDeviceIdentified(deviceIdentified);
        List<ConfirmResult> confirmResults = getResumeConfirmResults(body.getConfirmResults(), basic, token);

        assistantMessage.saveUserConfirmResultMetadata(confirmResults);
        applyConfirmResults(assistantMessage, confirmResults);
        getAgentRecordStore().saveMessage(assistantMessage, deviceIdentified);

        agentSseStreamPublishResolver.publishStreamStart(assistantMessage.getAgentConversationId(), assistantMessage.getId());

        AgentChatBasicResponseBody result = new AgentChatBasicResponseBody();
        result.setAssistantMessageId(assistantMessage.getId());
        result.setUserMessageId(assistantMessage.getParentId());

        return result;
    }

    protected abstract AbstractFilesystem openRecords(String deviceIdentified);

    @Override
    public AgentRecordStore getAgentRecordStore() {
        return new FilesystemAgentRecordStore(this::openRecords);
    }
}
