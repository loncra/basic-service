package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentChatResponseBody extends AgentChatBasicResponseBody {

    @Serial
    private static final long serialVersionUID = 8249090608898653032L;

    private AgentConversationEntity conversation;
}
