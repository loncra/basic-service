package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.RequireUserConfirmEvent;
import io.agentscope.core.message.ToolUseBlock;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class RequestUserConfirmEventResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    protected List<ToolCallBlockContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {

        RequireUserConfirmEvent confirmEvent = CastUtils.cast(event);

        List<ToolCallBlockContentMetadata> result = new LinkedList<>();
        for (ToolUseBlock toolUseBlock : confirmEvent.getToolCalls()) {
            ToolCallBlockContentMetadata metadata = new ToolCallBlockContentMetadata();
            metadata.setId(toolUseBlock.getId());
            metadata.setHitlStatus(toolUseBlock.getState());
            result.add(metadata);
        }

        return result;
    }

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        ToolCallBlockContentMetadata toolCallMetadata = assistant.obtainBlock(content.getId(), AgentMessageContentTypeEnum.TOOL_CALL);
        toolCallMetadata.setHitlStatus(content.getHitlStatus());
        assistant.updateContent(toolCallMetadata);
        return false;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return RequireUserConfirmEvent.class.isAssignableFrom(event.getClass());
    }
}
