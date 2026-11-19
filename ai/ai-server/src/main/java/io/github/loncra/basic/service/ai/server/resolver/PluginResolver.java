package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;

import java.util.List;

public interface PluginResolver {

    List<InstructionMessageMetadata> getUserAgentMessageHits(AgentMessageEntity userMessage);

    void applyAgentPluginInstruction(
            AgentPluginInstructionContext context,
            List<InstructionMessageMetadata> hits
    );

    PluginTargetTypeEnum getTargetType();

    void applyAgentHubInstall(AiUserPluginInstallEntity entity);

    AbstractUserPluginInstallMetadata getAgentHubInstallMetadata(Long packageId);

    /*default List<UserPluginMetadata> getWorkspacePluginInstall(AgentConversationEntity workspaceEntity) {
        Object specificsObject = workspaceEntity.getMetadata().getOrDefault(AgentHubService.WORKSPACE_PLUGIN_KEY, new LinkedList<>());
        List<Map<String, Object>> specifics = CastUtils.cast(specificsObject);
        return specifics.stream()
                .map(m -> CastUtils.cast(m, UserPluginMetadata.class))
                .filter(m -> m.getTargetType().equals(getTargetType()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    default boolean addWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity,
            UserPluginMetadata pluginMetadata
    ) {
        List<UserPluginMetadata> exists = getWorkspacePluginInstall(workspaceEntity);
        String id = UserPluginInstallResult.toUniqueIdentification(pluginMetadata);
        if (exists.stream().anyMatch(m -> UserPluginInstallResult.toUniqueIdentification(m).equals(id))) {
            return false;
        }
        exists.add(pluginMetadata);
        workspaceEntity.getMetadata().put(AgentHubService.WORKSPACE_PLUGIN_KEY, exists);
        return true;
    }

    default boolean removeWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity,
            Long packageId,
            PluginTargetTypeEnum targetType
    ) {
        List<UserPluginMetadata> exists = getWorkspacePluginInstall(workspaceEntity);
        String id = UserPluginInstallResult.toUniqueIdentification(packageId, targetType);
        if (exists.stream().anyMatch(m -> UserPluginInstallResult.toUniqueIdentification(m).equals(id))) {
            return false;
        }
        exists.removeIf(m -> UserPluginInstallResult.toUniqueIdentification(m).equals(id));
        workspaceEntity.getMetadata().put(AgentHubService.WORKSPACE_PLUGIN_KEY, exists);

        return true;
    }*/

    PluginPackageMetadata getPluginPackage(Long packageId);
}
