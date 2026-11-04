package io.github.loncra.basic.service.ai.server.resolver.channel;

import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.IdEntity;
import lombok.Setter;
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
            AgentMessageEntity assistant,
            ModelResolverMetadata modelResolverMetadata,
            String workspacePath
    ) {
        return HarnessAgent.builder()
                .name(assistant.getType().toString())
                .sysPrompt(aiAppConfig.getSystemPrompt())
                .model(modelResolverMetadata.getModel())
                .toolkit(modelResolverMetadata.getToolkit())
                .enableMetaTool(true)
                .stateStore(resolveStateStore())
                .skillRepository(new FileSystemSkillRepository(Path.of(skillConfig.getPath())))
                .compaction(aiAppConfig.toCompactionConfig())
                .workspace(workspacePath)
                .abstractFilesystem(createFilesystem(assistant, workspacePath))
                .middlewares(middlewares);
    }

    protected HarnessAgent finishAgent(HarnessAgent.Builder builder, AgentMessageEntity assistant) {
        String environmentMemory = createEnvironmentMemory(assistant.getDeviceIdentified(), assistant.getPrincipal());
        if (Objects.nonNull(environmentMemory)) {
            builder.environmentMemory(environmentMemory);
        }
        if (!AgentChatTypeEnum.ASK.equals(assistant.getType())) {
            builder.enablePlanMode()
                    .planFileDirectory(assistant.getType().toString());
        }
        return builder.build();
    }

    protected AgentStateStore resolveStateStore() {
        return agentStateStore;
    }

    protected String createEnvironmentMemory(String deviceIdentified, String principal) {
        return null;
    }
}
