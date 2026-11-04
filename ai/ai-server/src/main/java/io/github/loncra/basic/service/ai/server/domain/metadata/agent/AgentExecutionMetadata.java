package io.github.loncra.basic.service.ai.server.domain.metadata.agent;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class AgentExecutionMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String workspaceKey;

    private String clientConversationId;

    private String deviceIdentified;

    private ExecutionChannelEnum channel;

    private AgentConversationEntity conversation;

    private AgentMessageEntity userMessage;

    private AgentMessageEntity assistantMessage;
}
