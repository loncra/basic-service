package io.github.loncra.basic.service.ai.server.interceptor.support;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.github.loncra.basic.service.ai.server.config.ConversationConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.interceptor.AgentStreamEventInterceptor;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public List<AgentAssistantMessageContent> postEventsStream(AgentMessageEntity assistant) {
        AgentConversationEntity conversation = agentConversationService.get(assistant.getAgentConversationId());
        if (Objects.isNull(conversation) || conversation.getGenerateName().toBoolean()) {
            return List.of();
        }
        AgentMessageEntity userMessage = agentMessageService.get(assistant.getParentId());

        ReActAgent agent = modelSettingService.getAgent(assistant.getModel());
        RuntimeContext context = RuntimeContext.builder()
                .put(SecurityContext.class, SecurityContextHolder.getContext())
                .build();
        String message = MessageFormat.format(conversationConfig.getGeneratePrompt(), userMessage.obtainUserText(), assistant.obtainAssistantAnswerText());
        Msg msg = agent.call(message, context).block(conversationConfig.getGenerateTimeout().toDuration());
        if (Objects.isNull(msg)){
            return List.of();
        }

        AgentTokenUsageContentMetadata usage = CastUtils.of(msg.getChatUsage(), AgentTokenUsageContentMetadata.class);
        usage.setTokenType(AgentMessageContentTypeEnum.GENERATE_CONVERSATION_NAME);
        assistant.saveAgentTokenUsageMetadata(usage);

        agentMessageService.lambdaUpdate()
                .set(AgentMessageEntity::getMetadata, assistant.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();

        conversation.setName(msg.getTextContent());
        conversation.setGenerateName(YesOrNo.Yes);
        agentConversationService.updateById(conversation);

        AgentTextContentMetadata content = AgentTextContentMetadata.of(
                AgentMessageContentTypeEnum.GENERATE_CONVERSATION_NAME,
                conversation.getId().toString(),
                conversation.getName()
        );

        return List.of(content);
    }
}
