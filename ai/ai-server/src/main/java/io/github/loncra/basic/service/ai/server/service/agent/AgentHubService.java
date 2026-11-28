package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.McpUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallLayerEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallResult;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginStatusRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.plugin.UserPluginMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentRecordStore;
import io.github.loncra.basic.service.ai.server.resolver.PluginResolver;
import io.github.loncra.basic.service.ai.server.resolver.channel.AbstractAgentChannelResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.ApiKeyModeEnum;
import io.github.loncra.basic.service.commons.enumerate.OwnershipEnum;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件广场：安装、卸载、启用/禁用与生效仲裁。
 *
 * <p>生效判定 = 安装记录 × 工作空间归属：企业行（tenant 级）只对企业归属空间生效，
 * 个人行（principal 级）对个人空间与客户端空间生效，并在企业空间作为叠加层；
 * 同一 (targetType, packageId) 冲突时按 企业行 &gt; 工作空间级 metadata &gt; 个人行 仲裁，
 * 指定工作空间安装只写 {@code metadata["plugin"]}，不落 tb_ai_user_plugin_install 行。
 * 运行时（Skill/MCP Toolkit）接入不在当前范围，见 docs/superpowers/plans/2026-11-24-plugin-install-model-design.md。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
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

    /**
     * 安装广场插件。全部范围按 scope 落 principal/tenant 行（幂等更新）；
     * 指定范围校验工作空间归属与 scope 一致后只写工作空间 metadata。
     */
    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = "installPlugin:[#token.name]:[#body.targetType]:[#body.packageId]:[#body.scope]")
    public List<UserPluginInstallResult> install(
            UserPluginRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        OwnershipEnum scope = Objects.requireNonNullElse(body.getScope(), OwnershipEnum.USER);
        if (ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(token.getType())) {
            SystemException.isTrue(OwnershipEnum.USER.equals(scope), "控制台用户不能安装企业插件");
        }
        if (OwnershipEnum.ENTERPRISE.equals(scope)) {
            SystemException.isTrue(PrincipalDetailsConstants.isEnterpriseOwner(token), "仅企业管理员可以安装企业插件");
        }

        PluginResolver pluginResolver = getPluginResolver(body.getTargetType());
        AiUserPluginInstallEntity entity = new AiUserPluginInstallEntity();
        entity.setTargetType(body.getTargetType());
        entity.setPackageId(body.getPackageId());
        entity.setScope(scope);
        entity.setWorkspaceScope(body.getWorkspaceScope());
        entity.setPrincipal(token.getName());
        entity.setTenantId(currentTenantId(token));

        if (PluginInstallWorkspaceScopeEnum.ALL.equals(body.getWorkspaceScope())) {
            pluginResolver.applyAgentHubInstall(entity, body.getMetadata());
            AiUserPluginInstallEntity existing = findInstallRow(token, body.getTargetType(), body.getPackageId(), scope);
            if (Objects.nonNull(existing)) {
                existing.setMetadata(entity.getMetadata());
                existing.setStatus(entity.getStatus());
                existing.setException(entity.getException());
                aiUserPluginInstallService.updateById(existing);
                return List.of(toResult(existing, null, layerOf(scope)));
            }
            cleanWorkspaceStaleInstalls(token, body.getTargetType(), body.getPackageId(), scope, deviceIdentified, List.of());
            aiUserPluginInstallService.insert(entity);
            return List.of(toResult(entity, null, layerOf(scope)));
        }

        if (PluginInstallWorkspaceScopeEnum.SPECIFIED.equals(body.getWorkspaceScope())) {
            SystemException.isTrue(CollectionUtils.isNotEmpty(body.getWorkspaces()), () -> new ServiceException("指定工作空间数据不能为空"));

            // 同一归属内 ALL 与 SPECIFIED 互斥：改存指定范围时删除原 ALL 落库行，
            // 否则 ALL 行继续对全部工作空间生效，与新写入的工作空间级 metadata 冲突
            AiUserPluginInstallEntity allRow = findInstallRow(token, body.getTargetType(), body.getPackageId(), scope);
            if (Objects.nonNull(allRow)) {
                aiUserPluginInstallService.deleteByEntity(allRow);
            }

            List<UserPluginInstallResult> result = new LinkedList<>();
            for (IdValueMetadata<String, ExecutionChannelEnum> workspace : body.getWorkspaces()) {
                AgentConversationEntity workspaceEntity = loadWorkspace(workspace, token, deviceIdentified);
                assertWorkspaceOwnership(workspaceEntity, scope);
                AiUserPluginInstallEntity workspaceInstall = new AiUserPluginInstallEntity();
                workspaceInstall.setTargetType(body.getTargetType());
                workspaceInstall.setPackageId(body.getPackageId());
                workspaceInstall.setScope(scope);
                workspaceInstall.setWorkspaceScope(body.getWorkspaceScope());
                pluginResolver.applyAgentHubInstall(workspaceInstall, workspaceInstallMetadata(body.getMetadata(), workspace.getMetadata()));
                UserPluginMetadata pluginMetadata = CastUtils.of(workspaceInstall, UserPluginMetadata.class);
                if (!addWorkspacePluginInstall(workspaceEntity, pluginMetadata)) {
                    continue;
                }
                agentManager.getChannelResolver(workspace.getValue())
                        .getAgentRecordStore()
                        .updateConversation(workspaceEntity, deviceIdentified);
                result.add(toResult(pluginMetadata, workspaceEntity.toIdNameValueMetadata(), PluginInstallLayerEnum.WORKSPACE));
            }

            cleanWorkspaceStaleInstalls(token, body.getTargetType(), body.getPackageId(), scope, deviceIdentified, body.getWorkspaces());
            return result;
        }

        throw new SystemException("暂不支持 [" + body.getWorkspaceScope() + "] 的工作空间范围安装插件");
    }

    /**
     * 卸载广场插件。全部范围删安装行（企业行任意 owner 可卸，个人行仅本人）；
     * 指定范围移除所选工作空间 metadata 中的安装项。
     */
    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = "installPlugin:[#token.name]:[#body.targetType]:[#body.packageId]:[#body.scope]")
    public void uninstall(
            UserPluginRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        OwnershipEnum scope = Objects.requireNonNullElse(body.getScope(), OwnershipEnum.USER);

        if (PluginInstallWorkspaceScopeEnum.ALL.equals(body.getWorkspaceScope())) {
            AiUserPluginInstallEntity entity = findInstallRow(token, body.getTargetType(), body.getPackageId(), scope);
            SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + body.getPackageId() + "] 的 [" + body.getTargetType().getName() + "] 插件安装"));
            assertRowOperator(entity, scope, token);
            aiUserPluginInstallService.deleteByEntity(entity);
            cleanWorkspaceStaleInstalls(token, body.getTargetType(), body.getPackageId(), scope, deviceIdentified, List.of());
            return;
        }

        if (PluginInstallWorkspaceScopeEnum.SPECIFIED.equals(body.getWorkspaceScope())) {
            if (OwnershipEnum.ENTERPRISE.equals(scope)) {
                SystemException.isTrue(PrincipalDetailsConstants.isEnterpriseOwner(token), "仅企业管理员可以卸载企业插件");
            }
            for (IdValueMetadata<String, ExecutionChannelEnum> workspace : body.getWorkspaces()) {
                AgentConversationEntity workspaceEntity = loadWorkspace(workspace, token, deviceIdentified);
                assertWorkspaceOwnership(workspaceEntity, scope);
                AgentRecordStore agentRecordStore = agentManager.getChannelResolver(workspace.getValue()).getAgentRecordStore();
                if (!removeWorkspacePluginInstall(workspaceEntity, body.getPackageId(), body.getTargetType())) {
                    continue;
                }
                agentRecordStore.updateConversation(workspaceEntity, deviceIdentified);
            }
            return;
        }

        throw new SystemException("暂不支持 [" + body.getWorkspaceScope() + "] 的工作空间范围卸载插件");
    }

    /**
     * 启用或禁用行级安装。禁用直接置 DISABLED（metadata/exception 保留，不参与仲裁）；
     * 启用仅对 MANUAL 鉴权的 MCP 重跑解析器试连回写状态，其余直接置 ACTIVATED
     * （Skill 的解析器会忽略存量 metadata 强钉最新版本，重跑会意外升级锁版）。
     */
    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = "installPlugin:[#token.name]:[#body.targetType]:[#body.packageId]:[#body.scope]")
    public UserPluginInstallResult updateInstallStatus(
            UserPluginStatusRequestBody body,
            AuditAuthenticationToken token
    ) {
        AiUserPluginInstallEntity entity = findInstallRow(token, body.getTargetType(), body.getPackageId(), body.getScope());
        SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + body.getPackageId() + "] 的 [" + body.getTargetType().getName() + "] 插件安装"));
        assertRowOperator(entity, body.getScope(), token);

        if (PluginInstallStatusEnum.DISABLED.equals(body.getStatus())) {
            entity.setStatus(PluginInstallStatusEnum.DISABLED);
            aiUserPluginInstallService.updateById(entity);
            return toResult(entity, null, layerOf(body.getScope()));
        }

        SystemException.isTrue(PluginInstallStatusEnum.ACTIVATED.equals(body.getStatus()), "仅支持启用或禁用操作");
        if (isManualAuthMcp(entity)) {
            PluginResolver pluginResolver = getPluginResolver(body.getTargetType());
            AiUserPluginInstallEntity probe = new AiUserPluginInstallEntity();
            probe.setTargetType(entity.getTargetType());
            probe.setPackageId(entity.getPackageId());
            probe.setScope(entity.getScope());
            probe.setWorkspaceScope(entity.getWorkspaceScope());
            pluginResolver.applyAgentHubInstall(probe, entity.getMetadata());
            entity.setStatus(probe.getStatus());
            entity.setException(probe.getException());
            entity.setMetadata(probe.getMetadata());
        } else {
            entity.setStatus(PluginInstallStatusEnum.ACTIVATED);
            entity.setException(null);
        }
        aiUserPluginInstallService.updateById(entity);
        return toResult(entity, null, layerOf(body.getScope()));
    }

    /**
     * 查询插件安装。传 workspaceId 返回该空间仲裁后的有效安装集（含 PENDING/FAILURE 供目录渲染禁用态）；
     * 不传返回原始行（企业行 + 个人行 + 各工作空间 metadata 安装），供 hub"我的插件"页使用。
     */
    public List<UserPluginInstallResult> findInstalls(
            AuditAuthenticationToken token,
            String deviceIdentified,
            String workspaceId
    ) {
        if (StringUtils.isNotBlank(workspaceId)) {
            AgentConversationEntity workspace = agentManager.findWorkspaces(token, deviceIdentified).stream()
                    .filter(w -> w.getId().equals(workspaceId))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("找不到工作空间 [" + workspaceId + "]"));
            return resolveEffectiveInstalls(workspace, token);
        }

        List<UserPluginInstallResult> result = new LinkedList<>();
        aiUserPluginInstallService.findRows(token, OwnershipEnum.ENTERPRISE, null).stream()
                .map(s -> toResult(s, null, PluginInstallLayerEnum.ENTERPRISE_ROW))
                .forEach(result::add);
        aiUserPluginInstallService.findRows(token, OwnershipEnum.USER, null).stream()
                .map(s -> toResult(s, null, PluginInstallLayerEnum.USER_ROW))
                .forEach(result::add);
        agentManager.findWorkspaces(token, deviceIdentified).forEach(workspace ->
                getWorkspacePluginInstall(workspace).stream()
                        .filter(s -> !PluginInstallStatusEnum.DISABLED.equals(s.getStatus()))
                        .map(s -> toResult(s, workspace.toIdNameValueMetadata(), PluginInstallLayerEnum.WORKSPACE))
                        .forEach(result::add));
        return result;
    }

    /**
     * 生效仲裁唯一来源：取数（企业行/工作空间 metadata/个人行）→ 适用域过滤（按工作空间归属）
     * → 冲突仲裁（企业行 &gt; 工作空间级 &gt; 个人行，DISABLED 不参与）→ 打 layer/status 标输出。
     */
    public List<UserPluginInstallResult> resolveEffectiveInstalls(
            AgentConversationEntity workspace,
            AuditAuthenticationToken token
    ) {
        Map<String, UserPluginInstallResult> effective = new LinkedHashMap<>();

        if (OwnershipEnum.ENTERPRISE.equals(
                Objects.requireNonNullElse(workspace.getOwnership(), OwnershipEnum.USER))) {
            aiUserPluginInstallService.findRows(token, OwnershipEnum.ENTERPRISE, null).stream()
                    .map(s -> toResult(s, null, PluginInstallLayerEnum.ENTERPRISE_ROW))
                    .forEach(r -> effective.put(uniqueKey(r), r));
        }

        getWorkspacePluginInstall(workspace).stream()
                .filter(s -> !PluginInstallStatusEnum.DISABLED.equals(s.getStatus()))
                .map(s -> toResult(s, workspace.toIdNameValueMetadata(), PluginInstallLayerEnum.WORKSPACE))
                .forEach(r -> effective.putIfAbsent(uniqueKey(r), r));

        aiUserPluginInstallService.findRows(token, OwnershipEnum.USER, null).stream()
                .map(s -> toResult(s, null, PluginInstallLayerEnum.USER_ROW))
                .forEach(r -> effective.putIfAbsent(uniqueKey(r), r));

        return List.copyOf(effective.values());
    }

    public List<UserPluginMetadata> getWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity
    ) {
        Object specificsObject = workspaceEntity.getMetadata().getOrDefault(WORKSPACE_PLUGIN_KEY, new LinkedList<>());
        List<Map<String, Object>> specifics = CastUtils.cast(specificsObject);
        return specifics.stream()
                .map(m -> CastUtils.convertValue(m, UserPluginMetadata.class))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private String uniqueKey(UserPluginMetadata pluginMetadata) {
        return UserPluginInstallResult.toUniqueIdentification(pluginMetadata.getPackageId(), pluginMetadata.getTargetType());
    }

    private PluginInstallLayerEnum layerOf(OwnershipEnum scope) {
        return OwnershipEnum.ENTERPRISE.equals(scope)
                ? PluginInstallLayerEnum.ENTERPRISE_ROW
                : PluginInstallLayerEnum.USER_ROW;
    }

    private String currentTenantId(AuditAuthenticationToken token) {
        return ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(token.getType())
                ? token.getName()
                : TenantContextHolder.get().getId().toString();
    }

    private AiUserPluginInstallEntity findInstallRow(
            AuditAuthenticationToken token,
            PluginTargetTypeEnum targetType,
            Long packageId,
            OwnershipEnum scope
    ) {
        if (OwnershipEnum.ENTERPRISE.equals(scope)) {
            return aiUserPluginInstallService.findEnterpriseInstall(currentTenantId(token), targetType, packageId);
        }
        return aiUserPluginInstallService.findUserInstallPluginByPrincipal(token.getName(), targetType, packageId, scope);
    }

    private void assertRowOperator(
            AiUserPluginInstallEntity entity,
            OwnershipEnum scope,
            AuditAuthenticationToken token
    ) {
        if (OwnershipEnum.ENTERPRISE.equals(scope)) {
            SystemException.isTrue(PrincipalDetailsConstants.isEnterpriseOwner(token), "仅企业管理员可以操作企业插件");
            return;
        }
        SystemException.isTrue(token.getName().equals(entity.getPrincipal()), () -> new ServiceException("不能操作其他用户的插件"));
    }

    private boolean isManualAuthMcp(AiUserPluginInstallEntity entity) {
        if (!PluginTargetTypeEnum.MCP.equals(entity.getTargetType())) {
            return false;
        }
        McpUserPluginInstallMetadata metadata = CastUtils.cast(entity.obtainMetadata());
        return Objects.nonNull(metadata) && ApiKeyModeEnum.MANUAL.equals(metadata.getAuthMode());
    }

    private void assertWorkspaceOwnership(
            AgentConversationEntity workspaceEntity,
            OwnershipEnum scope
    ) {
        OwnershipEnum ownership = Objects.requireNonNullElse(
                workspaceEntity.getOwnership(),
                OwnershipEnum.USER
        );
        SystemException.isTrue(
                ownership.equals(scope),
                () -> new ServiceException("工作空间归属 [" + ownership.getName() + "] 与安装范围 [" + scope.getName() + "] 不一致")
        );
    }

    private AgentConversationEntity loadWorkspace(
            IdValueMetadata<String, ExecutionChannelEnum> workspace,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        AgentConversationEntity workspaceEntity = agentManager.getChannelResolver(workspace.getValue())
                .getAgentRecordStore()
                .getConversation(token.getName(), workspace.getId(), deviceIdentified);
        workspaceEntity = Objects.requireNonNull(workspaceEntity, "找不到 ID 为 [" + workspace.getId() + "] 的工作空间");
        SystemException.isTrue(
                AgentConversationTypeEnum.WORKSPACE_TYPES.contains(workspaceEntity.getType()),
                "该工作空间的类型为会话类型，无法安装插件"
        );
        return workspaceEntity;
    }

    /**
     * 清理与安装归属一致的工作空间中该插件的 metadata 残留（keep 内的工作空间跳过），跨归属不清理
     */
    private void cleanWorkspaceStaleInstalls(
            AuditAuthenticationToken token,
            PluginTargetTypeEnum targetType,
            Long packageId,
            OwnershipEnum scope,
            String deviceIdentified,
            List<IdValueMetadata<String, ExecutionChannelEnum>> keep
    ) {
        UserPluginMetadata marker = new UserPluginMetadata();
        marker.setTargetType(targetType);
        marker.setPackageId(packageId);
        agentManager.findWorkspaces(token, deviceIdentified)
                .stream()
                .filter(w -> Objects.requireNonNullElse(w.getOwnership(), OwnershipEnum.USER).equals(scope))
                .filter(w -> keep.stream().noneMatch(k -> k.getId().equals(w.getId())))
                .forEach(w -> removeWorkspacePluginInstall(marker, w, deviceIdentified));
    }

    private UserPluginInstallResult toResult(
            UserPluginMetadata pluginMetadata,
            IdNameValueMetadata<String, ExecutionChannelEnum> workspace,
            PluginInstallLayerEnum layer
    ) {
        UserPluginInstallResult result = CastUtils.of(pluginMetadata, UserPluginInstallResult.class);
        PluginPackageMetadata pluginPackage = getPluginResolver(pluginMetadata.getTargetType()).getPluginPackage(pluginMetadata.getPackageId());
        result.setPluginPackage(CastUtils.of(pluginPackage, PluginPackageMetadata.class));
        result.setWorkspace(workspace);
        result.setLayer(layer);
        return result;
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

    /**
     * 组装单个工作空间的安装 metadata：该空间 {@code metadata["releaseVersion"]}（per-workspace 锁版）优先，
     * 其次沿用请求顶层 metadata（如管理保存预填的锁定版本），都缺省时由解析器回落最新版本。
     */
    private Map<String, Object> workspaceInstallMetadata(Map<String, Object> bodyMetadata, Map<String, Object> workspaceMetadata) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (Objects.nonNull(bodyMetadata)) {
            merged.putAll(bodyMetadata);
        }
        Object releaseVersion = Objects.isNull(workspaceMetadata) ? null : workspaceMetadata.get("releaseVersion");
        if (Objects.nonNull(releaseVersion) && StringUtils.isNotBlank(Objects.toString(releaseVersion, null))) {
            merged.put("releaseVersion", releaseVersion);
        }
        return merged;
    }

    private boolean addWorkspacePluginInstall(
            AgentConversationEntity workspaceEntity,
            UserPluginMetadata pluginMetadata
    ) {
        List<UserPluginMetadata> current = getWorkspacePluginInstall(workspaceEntity);

        String id = UserPluginInstallResult.toUniqueIdentification(pluginMetadata);
        current.removeIf(s -> s.getTargetType().equals(pluginMetadata.getTargetType()) && UserPluginInstallResult.toUniqueIdentification(s).equals(id));

        current.add(pluginMetadata);
        workspaceEntity.getMetadata().put(WORKSPACE_PLUGIN_KEY, current);
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
        workspaceEntity.getMetadata().put(WORKSPACE_PLUGIN_KEY, exists);
        return true;
    }
}
