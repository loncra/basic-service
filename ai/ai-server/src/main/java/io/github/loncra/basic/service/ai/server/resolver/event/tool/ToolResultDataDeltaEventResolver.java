package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultDataDeltaEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class ToolResultDataDeltaEventResolver extends UpdateToolCallDataResolver {

    @Override
    protected ToolCallBlockContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolResultDataDeltaEvent resultDataDeltaEvent = CastUtils.cast(event);
        ToolCallBlockContentMetadata result = new ToolCallBlockContentMetadata();
        result.setId(resultDataDeltaEvent.getToolCallId());
        result.setOutput(resultDataDeltaEvent.getData());
        return result;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolResultDataDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected boolean postUpdate(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        return false;
    }
}
