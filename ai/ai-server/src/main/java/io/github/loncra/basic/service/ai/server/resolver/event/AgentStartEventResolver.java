package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentStartEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusContentMetadata;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentStartEventResolver implements AgentEventResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return AgentStartEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public List<AbstractAssistantMessageContentMetadata> process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        AgentStatusContentMetadata result = new AgentStatusContentMetadata();
        result.setId(assistant.getAgentConversationId().toString());
        result.setStatus(assistant.getStatus());
        result.setAssistantMessageId(assistant.getId());

        return List.of(result);
    }
}
