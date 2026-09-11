package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;

import java.util.List;

public interface AgentPluginMessageContentResolver {

    List<InstructionMessageMetadata> getHits(AgentMessageEntity userMessage);

    void apply(
            AgentPluginInstructionContext context,
            List<InstructionMessageMetadata> hits
    );
}
