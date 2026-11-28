package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;

import java.util.List;
import java.util.Map;

public interface PluginResolver {

    List<InstructionMessageMetadata> getUserAgentMessageHits(AgentMessageEntity userMessage);

    void applyAgentPluginInstruction(
            AgentPluginInstructionContext context,
            List<InstructionMessageMetadata> hits
    );

    PluginTargetTypeEnum getTargetType();

    void applyAgentHubInstall(AiUserPluginInstallEntity entity, Map<String, Object> bodyMetadata);

    AbstractUserPluginInstallMetadata getAgentHubInstallMetadata(Long packageId);

    PluginPackageMetadata getPluginPackage(Long packageId);
}
