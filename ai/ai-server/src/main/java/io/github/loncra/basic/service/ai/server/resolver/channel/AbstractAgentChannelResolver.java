package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.basic.service.ai.server.resolver.state.RpcJsonFileAgentStateStore;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Setter;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Setter(onMethod_ = {@Autowired})
public abstract class AbstractAgentChannelResolver implements AgentChannelResolver {

    protected ModelSettingService modelSettingService;

    protected AgentConversationService conversationService;

    protected AgentMessageService messageService;

    protected AiAppConfig aiAppConfig;

    protected SkillConfig skillConfig;

    protected List<MiddlewareBase> middlewares;

    protected AgentStateStore agentStateStore;

    protected AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    protected ModelSettingMetadata resolveModelMetadata(Long modelId) {
        ModelSettingEntity model = Objects.requireNonNull(modelSettingService.get(modelId), "找不到 ID 为 [" + modelId + "] 的模型数据");
        return CastUtils.of(model, ModelSettingMetadata.class);
    }

    protected void prepareConversation(AgentConversationEntity conversation, AgentChatRequestBody body, ModelSettingMetadata modelMetadata) {
        conversation.setStatus(AgentChatStatusEnum.READY);
        conversation.setLastModel(modelMetadata);
        conversation.setLastChatType(body.getType());
    }

    protected AgentMessageEntity buildUserMessage(
            AgentChatRequestBody body,
            ModelSettingMetadata modelMetadata,
            Long conversationId,
            String principal
    ) {
        AgentMessageEntity userMessage = CastUtils.of(body, AgentMessageEntity.class);
        userMessage.setModel(modelMetadata);
        userMessage.setRole(AgentMessageRoleEnum.USER);
        userMessage.setPrincipal(principal);
        userMessage.setStatus(AgentChatStatusEnum.READY);
        userMessage.setAgentConversationId(conversationId);
        return userMessage;
    }

    protected AgentMessageEntity buildAssistantMessage(AgentMessageEntity userMessage, Long conversationId) {
        AgentMessageEntity assistantMessage = CastUtils.of(userMessage, AgentMessageEntity.class, IdEntity.ID_FIELD_NAME);
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

    protected HarnessAgent.Builder baseBuilder(
            AgentExecutionMetadata execution,
            ModelResolverMetadata modelResolverMetadata,
            String workspacePath
    ) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        return HarnessAgent.builder()
                .name(assistant.getType().toString())
                .sysPrompt(aiAppConfig.getSystemPrompt())
                .model(modelResolverMetadata.getModel())
                .toolkit(modelResolverMetadata.getToolkit())
                .enableMetaTool(true)
                .stateStore(resolveStateStore(execution))
                .skillRepository(new FileSystemSkillRepository(Path.of(skillConfig.getPath())))
                .compaction(aiAppConfig.toCompactionConfig())
                .workspace(workspacePath)
                .abstractFilesystem(createFilesystem(execution, workspacePath))
                .middlewares(middlewares);
    }

    protected HarnessAgent finishAgent(HarnessAgent.Builder builder, AgentExecutionMetadata execution) {
        AgentMessageEntity assistant = execution.getAssistantMessage();
        String environmentMemory = createEnvironmentMemory(execution.getDeviceIdentified(), assistant.getPrincipal());
        if (Objects.nonNull(environmentMemory)) {
            builder.environmentMemory(environmentMemory);
        }
        if (!AgentChatTypeEnum.ASK.equals(assistant.getType())) {
            builder.enablePlanMode()
                    .planFileDirectory(assistant.getType().toString());
        }
        return builder.build();
    }

    protected abstract AgentStateStore resolveStateStore(BasicAgentExecutionMetadata execution);

    protected String createEnvironmentMemory(String deviceIdentified, String principal) {
        return null;
    }

    protected List<ConfirmResult> getResumeConfirmResults(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        BasicAgentExecutionMetadata basic = new BasicAgentExecutionMetadata();
        basic.setChannel(body.getChannel());
        basic.setClientConversationId(body.getClientConversationId());
        basic.setDeviceIdentified(deviceIdentified);

        AgentState state = resolveStateStore(basic)
                .get(
                        obtainUserId(token),
                        body.getClientConversationId(),
                        RpcJsonFileAgentStateStore.AGENT_STATE_KEY,
                        AgentState.class
                )
                .orElseThrow(() -> new SystemException("找不到会话 [" + body.getClientConversationId() + "] 的执行状态，无法恢复确认"));

        List<ToolUseBlock> askingToolCalls = findAskingToolCalls(state);
        SystemException.isTrue(!askingToolCalls.isEmpty(), "会话 [" + body.getClientConversationId() + "] 没有等待确认的工具调用");

        List<ConfirmResult> confirmResults = new LinkedList<>();
        for (ToolUseBlock toolUseBlock : askingToolCalls) {
            Boolean confirm = body.getConfirmResults()
                    .stream()
                    .filter(s -> s.getToolCallId().equals(toolUseBlock.getId()))
                    .findFirst()
                    .map(AgentResumeRequestBody.ConfirmResult::isConfirmed)
                    .orElseThrow(() -> new SystemException("ID 为 [" + toolUseBlock.getId() + "] 的工具需要确认"));
            confirmResults.add(new ConfirmResult(confirm, toolUseBlock));
        }
        return confirmResults;
    }

    protected static void applyConfirmResults(AgentMessageEntity assistantMessage, List<ConfirmResult> confirmResults) {
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

    private String obtainUserId(AuditAuthenticationToken token) {
        return Strings.CS.replace(token.getName(), CacheProperties.DEFAULT_SEPARATOR, CastUtils.UNDERSCORE);
    }
}
