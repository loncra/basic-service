package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.github.loncra.basic.service.ai.api.enumerate.AgentToolCallStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentToolCallContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentToolCallEndContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ToolResultEndEventResolver extends AbstractAgentEventResolver<AgentToolCallEndContentMetadata> {

    @Override
    public boolean isSupport(AgentEvent event) {
        return ToolResultEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected AgentToolCallEndContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ToolResultEndEvent end = CastUtils.cast(event);
        AgentToolCallEndContentMetadata tool = new AgentToolCallEndContentMetadata();
        tool.setEndTime(Instant.now());
        tool.setId(end.getReplyId());
        tool.setName(end.getToolCallName() + CastUtils.UNDERSCORE + end.getToolCallId());
        tool.setStatus(AgentToolCallStatusEnum.DONE);
        tool.setResultState(end.getState().name());

        return tool;
    }

    @Override
    public boolean postPublish(
            AgentToolCallEndContentMetadata content,
            AgentMessageEntity assistant
    ) {
        ToolResultEndEvent end = CastUtils.cast(content.getEventSource());
        AgentToolCallContentMetadata metadata = assistant.obtainBlock(end.getReplyId(), AgentToolCallContentMetadata.class);
        if (Objects.isNull(metadata)) {
            return false;
        }
        BeanUtils.copyProperties(content, metadata);
        assistant.updateContent(metadata);
        updateAssistantContent(assistant);
        return true;
    }
}
