package io.github.loncra.basic.service.ai.server.domain.body;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AgentChatBasicResponseBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 8249090608898653032L;

    private Long userMessageId;

    private Long assistantMessageId;
}
