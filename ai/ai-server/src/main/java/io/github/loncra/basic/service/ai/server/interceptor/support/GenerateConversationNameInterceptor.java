package io.github.loncra.basic.service.ai.server.interceptor.support;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.github.loncra.basic.service.ai.server.config.ConversationConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.interceptor.AgentStreamEventInterceptor;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentRecordStore;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateConversationNameInterceptor implements AgentStreamEventInterceptor {

    private final ModelSettingService modelSettingService;

    private final ConversationConfig conversationConfig;

    @Override
    public List<AbstractAssistantMessageContentMetadata> postEventsStream(
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        if (AgentChatStatusEnum.STOPPED.equals(assistant.getStatus())) {
            log.info("话题 {} 用户已手动停止, 跳过本次生成名称流程", assistant.getAgentConversationId());
            return List.of();
        }
        AgentExecutionMetadata executionMetadata = context.get(AgentExecutionMetadata.class);
        AgentChannelResolver agentChannelResolver = context.get(AgentChannelResolver.class);
        AgentRecordStore records = agentChannelResolver.getAgentRecordStore();
        AgentConversationEntity conversation = records.getConversation(
                executionMetadata.getToken().getName(),
                assistant.getAgentConversationId(),
                executionMetadata.getDeviceIdentified()
        );
        if (Objects.isNull(conversation) || !ExecuteStatus.PENDING_STATUS.contains(conversation.getGenerateNameStatus())) {
            log.info("话题 {} 正在生成名称, 跳过本次生成名称流程", assistant.getAgentConversationId());
            return List.of();
        }

        records.updateConversationGenerateNameStatus(
                conversation.getId(),
                ExecuteStatus.Processing,
                executionMetadata
        );

        AgentMessageEntity userMessage = records.getMessage(
                executionMetadata.getToken().getName(),
                assistant.getParentId(),
                executionMetadata.getDeviceIdentified()
        );
        ModelResolverMetadata metadata = modelSettingService.getModelMetadata(assistant.getModel(), null);

        ReActAgent.Builder builder = ReActAgent.builder()
                .name(GenerateConversationNameInterceptor.class.getSimpleName())
                .model(metadata.getModel());
        try (ReActAgent agent = builder.build()) {

            String message = MessageFormat.format(conversationConfig.getGeneratePrompt(), userMessage.obtainUserText(), assistant.obtainAssistantAnswerText());
            Msg msg = agent.call(message, context).block(conversationConfig.getGenerateTimeout().toDuration());
            if (Objects.isNull(msg)) {
                return List.of();
            }

            AgentTokenUsageMetadata usage = CastUtils.of(msg.getChatUsage(), AgentTokenUsageMetadata.class);
            usage.setUsageType(AgentMessageContentTypeEnum.GENERATE_CONVERSATION_NAME);
            usage.setId(conversation.getId());
            usage.setAssistantMessageId(assistant.getId());

            assistant.saveAgentTokenUsageMetadata(usage);

            records.updateAssistantMetadata(
                    assistant,
                    executionMetadata
            );

            conversation.setName(StringUtils.defaultIfEmpty(msg.getTextContent(), conversation.getName()));
            conversation.setGenerateNameStatus(ExecuteStatus.Success);

            CustomizeMetadata content = new CustomizeMetadata();
            content.setEventType(AgentMessageContentTypeEnum.GENERATE_CONVERSATION_NAME);
            content.setId(conversation.getId());
            content.getMetadata().put(NameEnum.FIELD_NAME, conversation.getName());
            content.setAssistantMessageId(assistant.getId());

            return List.of(content, usage);
        } catch (Exception e) {
            log.error("会话 [{}] 生成名称失败", assistant.getAgentConversationId(), e);
            conversation.setGenerateNameStatus(ExecuteStatus.Failure);
            return List.of();
        } finally {
            records.updateGenerateName(conversation, executionMetadata);
        }
    }
}
