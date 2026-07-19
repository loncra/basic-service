package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentWorkspaceResponseBody extends AgentWorkspaceEntity {

    @Serial
    private static final long serialVersionUID = -2614273699959063299L;

    private List<AgentConversationEntity> conversations = new LinkedList<>();
}
