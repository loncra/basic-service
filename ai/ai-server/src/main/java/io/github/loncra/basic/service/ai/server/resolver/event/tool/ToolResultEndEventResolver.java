package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.message.ToolResultState;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ToolResultEndEventResolver extends UpdateToolCallDataResolver {

    @Override
    protected List<ToolCallBlockContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolResultEndEvent endEvent = CastUtils.cast(event);

        ToolCallBlockContentMetadata result = new ToolCallBlockContentMetadata();
        result.setId(endEvent.getToolCallId());
        result.setName(endEvent.getToolCallName());
        result.setStatus(ToolResultState.SUCCESS.equals(endEvent.getState()) ? AgentBlockStatusEnum.DONE : AgentBlockStatusEnum.FAILED);
        result.setEndTime(Instant.now());
        result.setResultState(endEvent.getState());

        return List.of(result);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolResultEndEvent.class.isAssignableFrom(event.getClass());
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
