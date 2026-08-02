package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultTextDeltaEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolResultTextDeltaEventResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    protected List<ToolCallBlockContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolResultTextDeltaEvent deltaEvent = CastUtils.cast(event);
        ToolCallBlockContentMetadata result = new ToolCallBlockContentMetadata();
        result.setId(deltaEvent.getToolCallId());
        result.setOutputText(deltaEvent.getDelta());
        return List.of(result);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolResultTextDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        ToolResultTextDeltaEvent event = CastUtils.cast(content.getEventSource());
        ToolCallBlockContentMetadata toolCallBlock = assistant.obtainBlock(event.getToolCallId(), AgentMessageContentTypeEnum.TOOL_CALL);
        appendDelta(content, toolCallBlock);
        assistant.updateContent(toolCallBlock);
        return false;
    }

    private static void appendDelta(
            ToolCallBlockContentMetadata content,
            ToolCallBlockContentMetadata toolCallBlock
    ) {
        String current = StringUtils.defaultIfEmpty(toolCallBlock.getOutputText(), StringUtils.EMPTY);
        String delta = StringUtils.defaultIfEmpty(content.getOutputText(), StringUtils.EMPTY);
        toolCallBlock.setOutputText(current + delta);
    }
}
