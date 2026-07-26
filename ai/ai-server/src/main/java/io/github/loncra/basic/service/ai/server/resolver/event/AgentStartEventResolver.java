package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentStartEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusChangeContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import org.springframework.stereotype.Component;

@Component
public class AgentStartEventResolver implements AgentEventResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return AgentStartEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public AgentAssistantMessageContent process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        AgentStatusChangeContentMetadata result = new AgentStatusChangeContentMetadata();
        result.setId(assistant.getAgentConversationId().toString());
        result.setStatus(AgentChatStatusEnum.RUNNING);
        return result;
    }
}
