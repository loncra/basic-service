package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.event.ToolResultDataDeltaEvent;

public class RequireUserConfirmEventResolver {


    public boolean isSupport(AgentEvent event) {
        return RequireUserConfirmEvent.class.isAssignableFrom(event.getClass());
    }
}
