package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallUserScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallResult;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.plugin.UserPluginMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentRecordStore;
import io.github.loncra.basic.service.ai.server.resolver.PluginResolver;
import io.github.loncra.basic.service.ai.server.resolver.channel.AbstractAgentChannelResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 将当前工作空间已激活的 Hub Skill 缓存到 {@code .skill-hub}，并返回第三仓。
 */
@Slf4j
@Service
@RequiredArgsConstructor
// FIXME 整体业务还存在问题，指定工作空间按照插件时，应该直接将插件信息，写进工作空间就好，没必要出现一个多余的 AiUserPluginInstallSpecificEntity 的存储
public class AgentHubService {

    public static final String WORKSPACE_PLUGIN_KEY = "plugin";

    private final AgentManager agentManager;

    private final AiUserPluginInstallService aiUserPluginInstallService;

    private final List<PluginResolver> pluginResolvers;

    public PluginResolver getPluginResolver(PluginTargetTypeEnum targetType) {
        return pluginResolvers.stream()
                .filter(s -> s.getTargetType().equals(targetType))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + targetType + "] 的插件解析器"));
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = "installPlugin:[#token.name]:[#body.targetType]:[#body.packageId]")
    public List<UserPluginInstallResult> install(
            UserPluginRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {

        AiUserPluginInstallEntity entity = new AiUserPluginInstallEntity();
        entity.setTargetType(body.getTargetType());
        entity.setPackageId(body.getPackageId());
        entity.setScope(body.getScope());

        entity.setWorkspaceScope(body.getWorkspaceScope());

        PluginResolver pluginResolver = getPluginResolver(body.getTargetType());
        pluginResolver.applyAgentHubInstall(entity, body.getMetadata());

        if (ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(token.getType())) {
            entity.setPrincipal(token.getName());
            entity.setTenantId(token.getName());
        } else {
            entity.setPrincipal(token.getName());
            entity.setTenantId(TenantContextHolder.get().getId().toString());
        }

        if (PluginInstallUserScopeEnum.ENTERPRISE.equals(body.getScope())) {
            SystemException.isTrue(ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE.equals(token.getType()), "非企业用户不能安装企业公共级插件");
        }

        if (PluginInstallWorkspaceScopeEnum.ALL.equals(body.getWorkspaceScope())) {
            AiUserPluginInstallEntity existing = aiUserPluginInstallService.findUserInstallPluginByPrincipal(
                    token.getName(),
                    body.getTargetType(),
                    body.getPackageId(),
                    body.getScope()
            );
            if (Objects.nonNull(existing)) {
                existing.setScope(body.getScope());
                aiUserPluginInstallService.updateById(existing);
                return List.of(toResult(entity));
            }
            // 检查所有工作空间，如果存在改插件安装，直接移除，因为用户级别的插件 > 工作空间级别的插件
            agentManager.findWorkspaces(token, deviceIdentified)
                    .forEach(w -> removeWorkspacePluginInstall(entity, w, deviceIdentified));

            aiUserPluginInstallService.insert(entity);
            return List.of(toResult(entity));
        } else if (PluginInstallWorkspaceScopeEnum.SPECIFIED.equals(body.getWorkspaceScope())) {
            SystemException.isTrue(CollectionUtils.isNotEmpty(body.getWorkspaces()), () -> new ServiceException("指定工作空间数据不能为空"));

            UserPluginMetadata pluginMetadata = CastUtils.of(entity, UserPluginMetadata.class);
            List<UserPluginInstallResult> result = new LinkedList<>();
            for (IdValueMetadata<String, ExecutionChannelEnum> workspace : body.getWorkspaces()) {
                AgentRecordStore agentRecordStore = agentManager.getChannelResolver(workspace.getValue())
                        .getAgentRecordStore();
                AgentConversationEntity workspaceEntity = agentRecordStore.getConversation(
                        token.getName(),
                        workspace.getId(),
                        deviceIdentified
                );
                SystemException.isTrue(
                        AgentConversationTypeEnum.WORKSPACE_TYPES.contains(workspaceEntity.getType()),
                        "该工作空间的类型为会话类型，无法安装插件"
                );

                if (!addWorkspacePluginInstall(workspaceEntity, pluginMetadata)) {
                    continue;
                }
                agentRecordStore.updateConversation(workspaceEntity, deviceIdentified);
                result.add(toResult(pluginMetadata, workspaceEntity.toIdNameValueMetadata()));
            }

            return result;
        }

        throw new SystemException("暂不支持 [" + body.getWorkspaceScope() + "] 的工作空间范围安装插件");
    }

    public void removeWorkspacePluginInstall(
            UserPluginMetadata entity,
            AgentConversationEntity workspace,
            String deviceIdentified
    ) {
        boolean flag = removeWorkspacePluginInstall(
                workspace,
                entity.getPackageId(),
                entity.getTargetType()
        );
        if (!flag) {
            return;
        }
        String kind = workspace.getMetadata()
                .getOrDefault(AbstractAgentChannelResolver.WORKSPACE_KIND_METADATA_KEY, StringUtils.EMPTY)
                .toString();
        ExecutionChannelEnum executionChannelEnum = ValueEnum.ofEnum(ExecutionChannelEnum.class, kind);
        agentManager.getChannelResolver(executionChannelEnum)
                .getAgentRecordStore()
                .updateConversation(workspace, deviceIdentified);
    }

    public List<UserPluginInstallResult> findActivatedInstallsByPrincipal(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        List<UserPluginInstallResult> result = aiUserPluginInstallService.findActivatedInstalls(token)
                .stream()
                .map(this::toResult)
                .collect(Collectors.toCollection(LinkedList::new));
        List<AgentConversationEntity> workspaces = agentManager.findWorkspaces(token, deviceIdentified);
        for (AgentConversationEntity workspace : workspaces) {
            getWorkspacePluginInstall(workspace).stream()
                    .map(s -> toResult(s, workspace.toIdNameValueMetadata()))
                    .forEach(result::add);
        }
        return result;
    }

    private UserPluginInstallResult toResult(
            UserPluginMetadata pluginMetadata
    ) {
        return toResult(pluginMetadata, null);
    }

    private UserPluginInstallResult toResult(
            UserPluginMetadata pluginMetadata,
            IdNameValueMetadata<String, ExecutionChannelEnum> workspace
    ) {
        UserPluginInstallResult result = CastUtils.of(pluginMetadata, UserPluginInstallResult.class);
        PluginPackageMetadata pluginPackage = getPluginResolver(pluginMetadata.getTargetType()).getPluginPackage(pluginMetadata.getPackageId());
        result.setPluginPackage(CastUtils.of(pluginPackage, PluginPackageMetadata.class));
        result.setWorkspace(workspace);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void uninstall(
            UserPluginRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        if (PluginInstallWorkspaceScopeEnum.ALL.equals(body.getWorkspaceScope())) {
            AiUserPluginInstallEntity entity = aiUserPluginInstallService.findUserInstallPluginByPrincipal(
                    token.getName(),
                    body.getTargetType(),
                    body.getPackageId(),
                    body.getScope()
            );
            SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + body.getPackageId() + "] 的 [" + body.getTargetType().getName() + "] 插件安装"));
            SystemException.isTrue(
                    token.getName().equals(entity.getPrincipal()),
                    () -> new ServiceException("不能卸载其他用户的插件")
            );
            aiUserPluginInstallService.deleteByEntity(entity);
        } else if (PluginInstallWorkspaceScopeEnum.SPECIFIED.equals(body.getWorkspaceScope())) {
            for (IdValueMetadata<String, ExecutionChannelEnum> workspace : body.getWorkspaces()) {
                AgentRecordStore agentRecordStore = agentManager.getChannelResolver(workspace.getValue())
                        .getAgentRecordStore();
                AgentConversationEntity workspaceEntity = agentRecordStore.getConversation(token.getName(), workspace.getId(), deviceIdentified);
                boolean flag = removeWorkspacePluginInstall(
                        workspaceEntity,
                        body.getPackageId(),
                        body.getTargetType()
                );
                if (!flag) {
                    continue;
                }
                agentRecordStore.updateConversation(workspaceEntity, deviceIdentified);
            }
        } else {
            throw new SystemException("暂不支持 [" + body.getWorkspaceScope() + "] 的工作空间范围卸载插件");
        }
    }

    public List<UserPluginMetadata> getWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity
    ) {
        Object specificsObject = workspaceEntity.getMetadata().getOrDefault(AgentHubService.WORKSPACE_PLUGIN_KEY, new LinkedList<>());
        List<Map<String, Object>> specifics = CastUtils.cast(specificsObject);
        return  specifics.stream()
                .map(m -> CastUtils.convertValue(m, UserPluginMetadata.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private boolean addWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity,
            UserPluginMetadata pluginMetadata
    ) {
        List<UserPluginMetadata> current = getWorkspacePluginInstall(workspaceEntity);

        String id = UserPluginInstallResult.toUniqueIdentification(pluginMetadata);
        current.removeIf(s -> s.getTargetType().equals(pluginMetadata.getTargetType()) && UserPluginInstallResult.toUniqueIdentification(s).equals(id));

        current.add(pluginMetadata);
        workspaceEntity.getMetadata().put(AgentHubService.WORKSPACE_PLUGIN_KEY, current);
        return true;
    }

    private boolean removeWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity,
            Long packageId,
            PluginTargetTypeEnum targetType
    ) {
        List<UserPluginMetadata> exists = getWorkspacePluginInstall(workspaceEntity);
        String id = UserPluginInstallResult.toUniqueIdentification(packageId, targetType);
        boolean removed = exists.removeIf(m -> UserPluginInstallResult.toUniqueIdentification(m).equals(id));
        if (!removed) {
            return false;
        }
        workspaceEntity.getMetadata().put(AgentHubService.WORKSPACE_PLUGIN_KEY, exists);
        return true;
    }
}
