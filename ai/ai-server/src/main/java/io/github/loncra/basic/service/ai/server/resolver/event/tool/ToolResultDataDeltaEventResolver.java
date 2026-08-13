package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultDataDeltaEvent;
import io.agentscope.core.message.ToolResultBlock;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ToolResultDataDeltaEventResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    protected List<ToolCallBlockContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolResultDataDeltaEvent deltaEvent = CastUtils.cast(event);
        ToolCallBlockContentMetadata result = new ToolCallBlockContentMetadata();
        result.setId(deltaEvent.getToolCallId());
        result.getOutputParts().add(deltaEvent.getData());
        /*if (deltaEvent.getData() ToolResultBlock.class.isAssignableFrom(deltaEvent.getData().getClass())) {
            ToolResultBlock block = CastUtils.cast(deltaEvent.getData());
            result.setResultState(block.getState());
            result.setName(block.getName());
        }*/
        return List.of(result);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolResultDataDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        ToolResultDataDeltaEvent event = CastUtils.cast(content.getEventSource());
        ToolCallBlockContentMetadata toolCallBlock = assistant.obtainBlock(event.getToolCallId(), AgentMessageContentTypeEnum.TOOL_CALL);
        toolCallBlock.getOutputParts().addAll(content.getOutputParts());

        assistant.updateContent(toolCallBlock);
        updateAssistantContent(assistant);

        return true;
    }
}
