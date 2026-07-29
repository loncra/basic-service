package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ThinkBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
public class ToolCallBlockRunningEventResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        ToolCallStartEvent startEvent = CastUtils.cast(content.getEventSource());
        ThinkBlockContentMetadata metadata = assistant.obtainBlock(startEvent.getReplyId(), AgentMessageContentTypeEnum.THINK);
        if (Objects.nonNull(metadata)) {
            metadata.setToolCall(content);
            assistant.updateContent(metadata);
        } else {
            assistant.updateContent(content);
        }

        return false;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolCallStartEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected ToolCallBlockContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolCallStartEvent startEvent = CastUtils.cast(event);
        ToolCallBlockContentMetadata toolCallContent = new ToolCallBlockContentMetadata();
        toolCallContent.setId(startEvent.getToolCallId());
        toolCallContent.setCreationTime(Instant.now());
        toolCallContent.setStatus(AgentBlockStatusEnum.READY);
        toolCallContent.setName(startEvent.getToolCallName());
        return toolCallContent;
    }
}
