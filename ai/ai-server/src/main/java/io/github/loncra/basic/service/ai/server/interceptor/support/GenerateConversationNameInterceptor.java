package io.github.loncra.basic.service.ai.server.interceptor.support;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.github.loncra.basic.service.ai.server.config.ConversationConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.interceptor.AgentStreamEventInterceptor;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.id.IdEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateConversationNameInterceptor implements AgentStreamEventInterceptor {

    private final AgentConversationService agentConversationService;

    private final AgentMessageService agentMessageService;

    private final ModelSettingService modelSettingService;

    private final ConversationConfig conversationConfig;

    @Override
    public List<AbstractAssistantMessageContentMetadata> postEventsStream(AgentMessageEntity assistant) {
        if (AgentChatStatusEnum.STOPPED.equals(assistant.getStatus())) {
            log.info("话题 {} 用户已手动停止, 跳过本次生成名称流程", assistant.getAgentConversationId());
            return List.of();
        }
        AgentConversationEntity conversation = agentConversationService.get(assistant.getAgentConversationId());
        if (Objects.isNull(conversation) || !ExecuteStatus.PENDING_STATUS.contains(conversation.getGenerateNameStatus())) {
            log.info("话题 {} 正在生成名称, 跳过本次生成名称流程", assistant.getAgentConversationId());
            return List.of();
        }

        agentConversationService.lambdaUpdate()
                .set(AgentConversationEntity::getGenerateNameStatus, ExecuteStatus.Processing.getValue())
                .eq(IdEntity::getId, conversation.getId())
                .update();

        AgentMessageEntity userMessage = agentMessageService.get(assistant.getParentId());
        ModelResolverMetadata metadata = modelSettingService.getModelMetadata(assistant.getModel(), null);

        ReActAgent.Builder builder = ReActAgent.builder()
                .name(GenerateConversationNameInterceptor.class.getSimpleName())
                .model(metadata.getModel());
        try (ReActAgent agent = builder.build()) {
            RuntimeContext context = RuntimeContext.builder()
                    .put(SecurityContext.class, SecurityContextHolder.getContext())
                    .build();
            String message = MessageFormat.format(conversationConfig.getGeneratePrompt(), userMessage.obtainUserText(), assistant.obtainAssistantAnswerText());
            Msg msg = agent.call(message, context).block(conversationConfig.getGenerateTimeout().toDuration());
            if (Objects.isNull(msg)) {
                return List.of();
            }

            AgentTokenUsageMetadata usage = CastUtils.of(msg.getChatUsage(), AgentTokenUsageMetadata.class);
            usage.setUsageType(AgentMessageContentTypeEnum.GENERATE_CONVERSATION_NAME);
            usage.setId(conversation.getId().toString());
            usage.setAssistantMessageId(assistant.getId());

            assistant.saveAgentTokenUsageMetadata(usage);

            agentMessageService.lambdaUpdate()
                    .set(AgentMessageEntity::getMetadata, assistant.obtainMetadataJsonString())
                    .eq(AgentMessageEntity::getId, assistant.getId())
                    .update();

            conversation.setName(StringUtils.defaultIfEmpty(msg.getTextContent(), conversation.getName()));
            conversation.setGenerateNameStatus(ExecuteStatus.Success);

            CustomizeMetadata content = new CustomizeMetadata();
            content.setEventType(AgentMessageContentTypeEnum.GENERATE_CONVERSATION_NAME);
            content.setId(conversation.getId().toString());
            content.getMetadata().put(NameEnum.FIELD_NAME, conversation.getName());
            content.setAssistantMessageId(assistant.getId());

            return List.of(content, usage);
        } catch (Exception e) {
            conversation.setGenerateNameStatus(ExecuteStatus.Failure);
            return List.of();
        } finally {
            agentConversationService.lambdaUpdate()
                    .set(AgentConversationEntity::getGenerateNameStatus, conversation.getGenerateNameStatus())
                    .set(AgentConversationEntity::getName, conversation.getName())
                    .eq(IdEntity::getId, conversation.getId())
                    .update();
        }
    }
}
