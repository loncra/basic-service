package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.message.ToolResultState;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class ToolResultStartEventResolver extends UpdateToolCallDataResolver {

    @Override
    protected ToolCallBlockContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolResultStartEvent resultStartEvent = CastUtils.cast(event);
        ToolCallBlockContentMetadata content = new ToolCallBlockContentMetadata();
        content.setId(resultStartEvent.getToolCallId());
        content.setName(resultStartEvent.getToolCallName());
        content.setStatus(AgentBlockStatusEnum.RUNNING);
        content.setResultState(ToolResultState.RUNNING);

        return content;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolResultStartEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected boolean postUpdate(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        updateAssistantContent(assistant);
        return true;
    }
}
