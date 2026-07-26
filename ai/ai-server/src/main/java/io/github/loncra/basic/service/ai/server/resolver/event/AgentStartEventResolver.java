package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentStartEvent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class AgentStartEventResolver extends AbstractAgentEventResolver<AgentTextContentMetadata> {

    @Override
    protected AgentTextContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        return AgentTextContentMetadata.of(AgentMessageContentTypeEnum.AGENT_START, event.getId(), event.getId());
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return AgentStartEvent.class.isAssignableFrom(event.getClass());
    }
}
