package io.github.loncra.basic.service.ai.server.domain.metadata.agent;

import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
public class AgentPluginInstructionContext {

    private Toolkit toolkit;

    private HarnessAgent agent;

    private List<String> mentionSummaries = new LinkedList<>();

    private AgentMessageEntity userMessage;

    private Long workspaceId;
}
