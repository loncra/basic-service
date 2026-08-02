package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolCallEndEvent;
import io.agentscope.core.message.ToolCallState;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ToolCallEndEventResolver extends UpdateToolCallDataResolver {


    @Override
    protected List<ToolCallBlockContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolCallEndEvent endEvent = CastUtils.cast(event);
        ToolCallBlockContentMetadata content = new ToolCallBlockContentMetadata();
        content.setId(endEvent.getToolCallId());
        content.setName(endEvent.getToolCallName());
        content.setStatus(AgentBlockStatusEnum.PENDING);
        content.setHitlStatus(ToolCallState.ALLOWED);
        content.setEndTime(Instant.now());

        return List.of(content);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolCallEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected boolean postUpdate(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {

        return false;
    }
}
