package io.github.loncra.basic.service.ai.server.domain;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.AgentEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssistantMessageStopEvent extends AgentEvent {

    private final Long assistantMessageId;
    private final String replyId;

    @Override
    public AgentEventType getType() {
        return AgentEventType.CUSTOM;
    }
}
