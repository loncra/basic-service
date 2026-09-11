package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;

import java.util.List;
import java.util.Objects;

public abstract class AbstractAgentPluginMessageContentResolver
        implements AgentPluginMessageContentResolver {

    protected abstract String getPrefix();

    @Override
    public List<InstructionMessageMetadata> getHits(AgentMessageEntity userMessage) {
        if (userMessage == null) {
            return List.of();
        }
        return InstructionMessageMetadata.ofList(userMessage.getContent()).stream()
                .filter(s -> Objects.equals(getPrefix(), s.getPrefix()))
                .toList();
    }
}
