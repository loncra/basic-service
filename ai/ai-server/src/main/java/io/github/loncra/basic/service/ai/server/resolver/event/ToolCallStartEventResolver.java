package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.github.loncra.basic.service.ai.api.enumerate.AgentToolCallStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentToolCallContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ToolCallStartEventResolver extends AbstractAgentEventResolver<AgentToolCallContentMetadata> {

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolCallStartEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected AgentToolCallContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolCallStartEvent start = CastUtils.cast(event);

        AgentToolCallContentMetadata tool = new AgentToolCallContentMetadata();
        tool.setCreationTime(Instant.now());
        tool.setId(start.getReplyId());
        tool.setName(start.getToolCallName() + CastUtils.UNDERSCORE + start.getToolCallId());
        tool.setStatus(AgentToolCallStatusEnum.RUNNING);

        return tool;
    }
}
