package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolCallDeltaEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolCallDeltaEventResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolCallDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected ToolCallBlockContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolCallDeltaEvent delta = CastUtils.cast(event);
        ToolCallBlockContentMetadata toolCallBlock = new ToolCallBlockContentMetadata();
        toolCallBlock.setId(delta.getToolCallId());
        toolCallBlock.setName(delta.getToolCallName());
        toolCallBlock.setValue(delta.getDelta());
        return toolCallBlock;
    }

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        ToolCallDeltaEvent event = CastUtils.cast(content.getEventSource());
        ToolCallBlockContentMetadata toolCallBlock = assistant.obtainBlock(event.getToolCallId(), AgentMessageContentTypeEnum.TOOL_CALL);
        appendDelta(content, toolCallBlock);

        return super.postPublish(content, assistant);
    }

    private static void appendDelta(
            ToolCallBlockContentMetadata content,
            ToolCallBlockContentMetadata toolCallBlock
    ) {
        String current = StringUtils.defaultIfEmpty(toolCallBlock.getValue(), StringUtils.EMPTY);
        String delta = StringUtils.defaultIfEmpty(content.getValue(), StringUtils.EMPTY);
        toolCallBlock.setValue(current + delta);
    }
}
