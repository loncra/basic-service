package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AgentChatResponseBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 8249090608898653032L;

    private Long userMessageId;

    private Long assistantId;

    private AgentConversationEntity conversation;
}
