package io.github.loncra.basic.service.ai.server.domain.metadata.agent;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentExecutionMetadata extends BasicAgentExecutionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private AgentConversationEntity conversation;

    private AgentMessageEntity userMessage;

    private AgentMessageEntity assistantMessage;
}
