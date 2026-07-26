package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.framework.commons.id.IdEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentEndEventResolver implements AgentEventResolver {

    private final AgentConversationService agentConversationService;

    @Override
    public boolean isSupport(AgentEvent event) {
        return AgentEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public AgentAssistantMessageContent process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        agentConversationService.lambdaUpdate()
                .set(AgentConversationEntity::getStatus, AgentChatStatusEnum.COMPLETED.getValue())
                .eq(IdEntity::getId, assistant.getAgentConversationId())
                .update();
        return AgentTextContentMetadata.of(
                AgentMessageContentTypeEnum.AGENT_END,
                assistant.getAgentConversationId().toString(),
                StringUtils.EMPTY
        );
    }
}
