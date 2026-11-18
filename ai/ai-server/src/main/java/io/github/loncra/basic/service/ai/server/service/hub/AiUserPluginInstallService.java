package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.util.SkillUtil;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.McpUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.SkillUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.*;
import io.github.loncra.basic.service.ai.server.dao.hub.AiUserPluginInstallDao;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.UserPluginInstallResult;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.*;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentRecordStore;
import io.github.loncra.basic.service.ai.server.service.agent.AgentManager;
import io.github.loncra.basic.service.auth.api.enumerate.TerminalTypeEnum;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * tb_ai_user_plugin_install 的业务逻辑
 *
 * <p>Table: tb_ai_user_plugin_install - 用户广场插件统一安装关联</p>
 *
 * @author maurice.chen
 * @see AiUserPluginInstallEntity
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiUserPluginInstallService extends BasicService<AiUserPluginInstallDao, AiUserPluginInstallEntity> {

    public static final String SOURCE = "hub";

    private final AiUserPluginInstallSpecificService aiUserPluginInstallSpecificService;

    private final AiSkillPackageService aiSkillPackageService;

    private final AiSkillReleaseService aiSkillReleaseService;

    private final AiMcpPackageService aiMcpPackageService;

    private final AgentManager agentManager;

    //private final List<AgentChannelResolver> channelResolvers;

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = "installPlugin:[#token.name]:[#body.targetType]:[#body.packageId]")
    public UserPluginInstallResult install(
            UserPluginInstallRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        SystemException.isTrue(Objects.nonNull(body.getTargetType()), () -> new ServiceException("插件类型不能为空"));
        SystemException.isTrue(Objects.nonNull(body.getPackageId()), () -> new ServiceException("插件目录不能为空"));
        SystemException.isTrue(Objects.nonNull(body.getWorkspaceScope()), () -> new ServiceException("工作空间范围不能为空"));

        List<IdNameMetadata> workspace = resolveWorkspace(body, token, deviceIdentified);
        AiUserPluginInstallEntity existing = findMinePackage(token.getName(), body.getTargetType(), body.getPackageId());
        if (Objects.nonNull(existing)) {
            existing.setWorkspaceScope(body.getWorkspaceScope());
            updateById(existing);
            replaceSpecific(existing.getId(), workspace);
            return toResult(existing, workspace);
        }

        AiUserPluginInstallEntity entity = new AiUserPluginInstallEntity();
        entity.setTargetType(body.getTargetType());
        entity.setPackageId(body.getPackageId());
        entity.setScope(body.getScope());

        entity.setWorkspaceScope(body.getWorkspaceScope());
        entity.setStatus(PluginInstallStatusEnum.ACTIVATED);
        entity.setMetadata(CastUtils.convertValue(createMetadata(body.getTargetType(), body.getPackageId()), CastUtils.MAP_TYPE_REFERENCE));

        if (ResourceSourceEnum.CONSOLE_SOURCE_VALUE.equals(token.getType())) {
            entity.setPrincipal(token.getName());
            entity.setTenantId(token.getName());
        }

        insert(entity);

        SystemException.isTrue(Objects.nonNull(entity.getId()), () -> new ServiceException("写入插件安装失败"));
        if (PluginInstallUserScopeEnum.USER.equals(entity.getScope())) {
            replaceSpecific(entity.getId(), workspace);
        }
        return toResult(entity, workspace);
    }

    public List<UserPluginInstallResult> find(AuditAuthenticationToken token, String deviceIdentified) {
        List<AiUserPluginInstallEntity> installs = lambdaQuery()
                .eq(AiUserPluginInstallEntity::getPrincipal, token.getName())
                //.eq(AiUserPluginInstallEntity::getScope, PluginInstallUserScopeEnum.USER)
                .orderByDesc(AiUserPluginInstallEntity::getId)
                .list();
        Map<Long, List<String>> workspaceIdsByInstall = groupingBySpecific(installs);
        Map<Long, PluginPackageMetadata> skillPackages = findPluginPackages(installs, PluginTargetTypeEnum.SKILL);
        Map<Long, PluginPackageMetadata> mcpPackages = findPluginPackages(installs, PluginTargetTypeEnum.SKILL);
        return installs.stream()
                .map(entity -> toResult(
                        entity,
                        getWorkspaceIdNameMetadata(token, new HashSet<>(workspaceIdsByInstall.getOrDefault(entity.getId(), List.of())), deviceIdentified),
                        resolvePluginPackage(entity, skillPackages, mcpPackages)
                ))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void uninstall(
            Long id,
            AuditAuthenticationToken token
    ) {
        AiUserPluginInstallEntity entity = get(id);
        SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + id + "] 的插件安装"));
        SystemException.isTrue(
                token.getName().equals(entity.getPrincipal()),
                () -> new ServiceException("不能卸载其他用户的插件")
        );
        deleteByEntity(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(AiUserPluginInstallEntity entity) {
        clearSpecific(entity.getId());
        return super.deleteByEntity(entity);
    }

    private AiUserPluginInstallEntity findMinePackage(
            String principal,
            PluginTargetTypeEnum targetType,
            Long packageId
    ) {
        return lambdaQuery()
                .eq(AiUserPluginInstallEntity::getPrincipal, principal)
                .eq(AiUserPluginInstallEntity::getTargetType, targetType)
                .eq(AiUserPluginInstallEntity::getPackageId, packageId)
                .eq(AiUserPluginInstallEntity::getScope, PluginInstallUserScopeEnum.USER)
                .one();
    }

    private Object createMetadata(
            PluginTargetTypeEnum targetType,
            Long packageId
    ) {
        if (PluginTargetTypeEnum.SKILL.equals(targetType)) {
            return createSkillMetadata(requireSkillPackage(packageId));
        } else if (PluginTargetTypeEnum.MCP.equals(targetType)) {
            return createMcpMetadata(requireMcpPackage(packageId));
        }
        throw new ServiceException("不支持的插件类型");
    }

    private SkillUserPluginInstallMetadata createSkillMetadata(AiSkillPackageEntity skillPackage) {
        SystemException.isTrue(
                StringUtils.isNotBlank(skillPackage.getLatestVersion()),
                () -> new ServiceException("Skill 目录 [" + skillPackage.getPackageKey() + "] 尚未打包版本，不能安装")
        );
        AiSkillReleaseEntity release = aiSkillReleaseService.lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, skillPackage.getId())
                .eq(AiSkillReleaseEntity::getReleaseVersion, skillPackage.getLatestVersion())
                .eq(AiSkillReleaseEntity::getEnabled, YesOrNo.Yes)
                .one();
        SystemException.isTrue(
                Objects.nonNull(release),
                () -> new ServiceException("Skill 目录 [" + skillPackage.getPackageKey() + "] 找不到已启用的版本 [" + skillPackage.getLatestVersion() + "]")
        );
        SkillUserPluginInstallMetadata metadata = new SkillUserPluginInstallMetadata();
        metadata.setReleaseId(release.getId());
        metadata.setReleaseVersion(release.getReleaseVersion());
        metadata.setContentHash(release.getContentHash());
        metadata.setUpdatePolicy(skillPackage.getDefaultUpdatePolicy());
        return metadata;
    }

    private McpUserPluginInstallMetadata createMcpMetadata(AiMcpPackageEntity mcpPackage) {
        McpUserPluginInstallMetadata metadata = new McpUserPluginInstallMetadata();
        if (Objects.nonNull(mcpPackage.getAuthMode())) {
            metadata.setAuthMode(mcpPackage.getAuthMode().getValue());
        }
        return metadata;
    }

    private AiSkillPackageEntity requireSkillPackage(Long packageId) {
        AiSkillPackageEntity entity = aiSkillPackageService.get(packageId);
        SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + packageId + "] 的 Skill 目录"));
        assertHubReleased(entity, "Skill 目录 [" + entity.getPackageKey() + "]");
        return entity;
    }

    private AiMcpPackageEntity requireMcpPackage(Long packageId) {
        AiMcpPackageEntity entity = aiMcpPackageService.get(packageId);
        SystemException.isTrue(Objects.nonNull(entity), () -> new ServiceException("找不到 ID 为 [" + packageId + "] 的 MCP 目录"));
        assertHubReleased(entity, "MCP 目录 [" + entity.getPackageKey() + "]");
        return entity;
    }

    private void assertHubReleased(
            PluginPackageMetadata entity,
            String label
    ) {
        SystemException.isTrue(
                PackageTypeEnum.HUB.equals(entity.getType()),
                () -> new ServiceException(label + "不是广场插件，不能安装")
        );
        SystemException.isTrue(
                DataStatusEnum.RELEASE.equals(entity.getStatus()),
                () -> new ServiceException(label + "尚未发布，不能安装")
        );
    }

    private List<IdNameMetadata> resolveWorkspace(
            UserPluginInstallRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        if (PluginInstallWorkspaceScopeEnum.ALL.equals(body.getWorkspaceScope())) {
            return List.of();
        }
        SystemException.isTrue(
                PluginInstallWorkspaceScopeEnum.SPECIFIED.equals(body.getWorkspaceScope()),
                () -> new ServiceException("不支持的工作空间范围")
        );
        Set<String> ids = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(body.getAgentConversationIds())) {
            body.getAgentConversationIds()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(ids::add);
        }
        SystemException.isTrue(CollectionUtils.isNotEmpty(ids), () -> new ServiceException("指定工作空间时至少选择一个工作空间"));
        return getWorkspaceIdNameMetadata(token, ids, deviceIdentified);
    }

    private @NonNull List<IdNameMetadata> getWorkspaceIdNameMetadata(
            AuditAuthenticationToken token,
            Set<String> conversationIds,
            String deviceIdentified
    ) {
        List<IdNameMetadata> workspaces = new LinkedList<>();
        String terminal = PrincipalDetailsConstants.getTerminal(token);
        AgentChannelResolver resolver =  agentManager.getChannelResolver(TerminalTypeEnum.BROWSER.toString().equals(terminal) ? ExecutionChannelEnum.CLOUD : ExecutionChannelEnum.CLIENT);

        AgentRecordStore agentRecordStore = resolver.getAgentRecordStore();
        for (String conversationId : conversationIds) {
            AgentConversationEntity conversation = agentRecordStore.getConversation(token.getName(), conversationId, deviceIdentified);
            SystemException.isTrue(Objects.nonNull(conversation), () -> new ServiceException("找不到 ID 为 [" + conversationId + "] 的工作空间"));
            SystemException.isTrue(
                    AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(conversation.getType())
                            || AgentConversationTypeEnum.CUSTOMIZE_WORKSPACE.equals(conversation.getType()),
                    () -> new ServiceException("只能绑定工作空间，不能绑定会话")
            );
            workspaces.add(IdNameMetadata.of(conversation.getId(), conversation.getName()));
        }
        return workspaces;
    }

    private void replaceSpecific(
            Long installId,
            List<IdNameMetadata> workspaces
    ) {
        clearSpecific(installId);
        for (IdNameMetadata workspace : workspaces) {
            AiUserPluginInstallSpecificEntity row = new AiUserPluginInstallSpecificEntity();
            row.setAiUserPluginInstallId(installId);
            row.setAgentConversationId(workspace.getId());
            aiUserPluginInstallSpecificService.insert(row);
        }
    }

    private void clearSpecific(Long installId) {
        if (Objects.isNull(installId)) {
            return;
        }
        aiUserPluginInstallSpecificService.lambdaUpdate()
                .eq(AiUserPluginInstallSpecificEntity::getAiUserPluginInstallId, installId)
                .remove();
    }

    private Map<Long, PluginPackageMetadata> findPluginPackages(List<AiUserPluginInstallEntity> installs, PluginTargetTypeEnum targetType) {
        List<Long> ids = packageIdsOf(installs, targetType);
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        if (PluginTargetTypeEnum.SKILL.equals(targetType)) {
            return aiSkillPackageService.lambdaQuery()
                    .in(AiSkillPackageEntity::getId, ids)
                    .list()
                    .stream()
                    .filter(item -> Objects.nonNull(item.getId()))
                    .collect(Collectors.toMap(AiSkillPackageEntity::getId, item -> item, (left, right) -> left));
        } else if (PluginTargetTypeEnum.MCP.equals(targetType)) {
            return aiMcpPackageService.lambdaQuery()
                    .in(AiMcpPackageEntity::getId, ids)
                    .list()
                    .stream()
                    .filter(item -> Objects.nonNull(item.getId()))
                    .collect(Collectors.toMap(AiMcpPackageEntity::getId, item -> item, (left, right) -> left));
        }

        throw new SystemException("不支持[" + targetType + "]的插件类型");
    }

    private List<Long> packageIdsOf(
            List<AiUserPluginInstallEntity> installs,
            PluginTargetTypeEnum targetType
    ) {
        return installs.stream()
                .filter(item -> targetType.equals(item.getTargetType()))
                .map(AiUserPluginInstallEntity::getPackageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private PluginPackageMetadata resolvePluginPackage(
            AiUserPluginInstallEntity entity,
            Map<Long, PluginPackageMetadata> skillPackages,
            Map<Long, PluginPackageMetadata> mcpPackages
    ) {
        if (Objects.isNull(entity.getPackageId())) {
            return null;
        }
        if (PluginTargetTypeEnum.SKILL.equals(entity.getTargetType())) {
            return skillPackages.get(entity.getPackageId());
        }
        if (PluginTargetTypeEnum.MCP.equals(entity.getTargetType())) {
            return mcpPackages.get(entity.getPackageId());
        }
        return null;
    }

    private UserPluginInstallResult toResult(
            AiUserPluginInstallEntity entity,
            List<IdNameMetadata> workspaces
    ) {
        Map<Long, PluginPackageMetadata> skillPackages = findPluginPackages(List.of(entity), PluginTargetTypeEnum.SKILL);
        Map<Long, PluginPackageMetadata> mcpPackages = findPluginPackages(List.of(entity), PluginTargetTypeEnum.MCP);
        return toResult(entity, workspaces, resolvePluginPackage(entity, skillPackages, mcpPackages));
    }

    private UserPluginInstallResult toResult(
            AiUserPluginInstallEntity entity,
            List<IdNameMetadata> workspaces,
            PluginPackageMetadata pluginPackage
    ) {
        UserPluginInstallResult result = CastUtils.convertValue(entity, UserPluginInstallResult.class);
        result.setWorkspaces(new LinkedList<>(workspaces));
        result.setPluginPackage(pluginPackage);
        return result;
    }

    /**
     * 当前用户在指定工作空间下已激活的 Hub 安装记录。
     *
     * @param workspaceId 指定工作空间 ID
     */
    public List<AiUserPluginInstallEntity> findActivatedInstalls(
            String workspaceId,
            PluginTargetTypeEnum targetType,
            AuditAuthenticationToken token
    ) {
        Map<String, AiUserPluginInstallEntity> effective = new LinkedHashMap<>();
        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        String enterpriseId = details.getMetadata()
                .getOrDefault(PrincipalDetailsConstants.ENTERPRISE_ID_KEY, StringUtils.EMPTY)
                .toString();
        if (StringUtils.isNoneEmpty(enterpriseId)) {
            lambdaQuery()
                    .eq(AiUserPluginInstallEntity::getStatus, PluginInstallStatusEnum.ACTIVATED)
                    .eq(AiUserPluginInstallEntity::getTenantId, enterpriseId)
                    .eq(AiUserPluginInstallEntity::getTargetType, targetType.getValue())
                    .eq(AiUserPluginInstallEntity::getScope, PluginInstallUserScopeEnum.ENTERPRISE)
                    .list()
                    .forEach(s -> effective.put(s.getId().toString(), s));
        }

        lambdaQuery()
                .eq(AiUserPluginInstallEntity::getStatus, PluginInstallStatusEnum.ACTIVATED)
                .eq(AiUserPluginInstallEntity::getScope, PluginInstallUserScopeEnum.USER)
                .eq(AiUserPluginInstallEntity::getTargetType, targetType.getValue())
                .eq(AiUserPluginInstallEntity::getPrincipal, token.getName())
                .list()
                .forEach(s -> effective.putIfAbsent(s.getId().toString(), s));

        List<AiUserPluginInstallEntity> installs = effective.values().stream()
                .toList();

        List<AiUserPluginInstallEntity> result = installs.stream()
                .filter(s -> PluginInstallWorkspaceScopeEnum.ALL.equals(s.getWorkspaceScope()))
                .collect(Collectors.toCollection(LinkedList::new));

        if (StringUtils.isNotEmpty(workspaceId)) {
            List<AiUserPluginInstallEntity> specified = installs.stream()
                    .filter(s -> PluginInstallWorkspaceScopeEnum.SPECIFIED.equals(s.getWorkspaceScope()))
                    .toList();

            Map<Long, List<String>> workspaceIdsByInstall = groupingBySpecific(specified);
            specified.stream()
                    .filter(install -> isActivatedInWorkspace(install, workspaceId, workspaceIdsByInstall))
                    .filter(install -> Objects.nonNull(install.getPackageId()))
                    .forEach(result::add);
        }

        return result;
    }

    private Map<Long, List<String>> groupingBySpecific(List<AiUserPluginInstallEntity> installs) {
        List<Long> installIds = installs.stream()
                .map(AiUserPluginInstallEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        return aiUserPluginInstallSpecificService.findByInstallIds(installIds)
                .stream()
                .collect(Collectors.groupingBy(
                        AiUserPluginInstallSpecificEntity::getAiUserPluginInstallId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                AiUserPluginInstallSpecificEntity::getAgentConversationId,
                                Collectors.toCollection(LinkedList::new)
                        )
                ));
    }

    private boolean isActivatedInWorkspace(
            AiUserPluginInstallEntity install,
            String workspaceId,
            Map<Long, List<String>> workspaceIdsByInstall
    ) {
        if (workspaceId == null) {
            return false;
        }
        List<String> bound = workspaceIdsByInstall.getOrDefault(install.getId(), List.of());
        return bound.contains(workspaceId);
    }

    public List<AgentSkill> findActivatedInstallAgentSkills(
            String sessionId,
            AuditAuthenticationToken token
    ) {

        List<AiUserPluginInstallEntity> installs = findActivatedInstalls(
                sessionId,
                PluginTargetTypeEnum.SKILL,
                token
        );
        List<AgentSkill> result = new ArrayList<>();
        for (AiUserPluginInstallEntity install : installs) {

            SkillUserPluginInstallMetadata metadata = CastUtils.convertValue(
                    install.getMetadata(), SkillUserPluginInstallMetadata.class);
            // 安装时钉版的 releaseId（metadata.releaseId，安装流程已写入）→ 读内容
            IdValueMetadata<Long, String> loaded = aiSkillReleaseService.loadSkill(metadata.getReleaseId());
            Map<String, String> resources = new LinkedHashMap<>();
            loaded.getMetadata().forEach((key, value) -> resources.put(key, value.toString()));
            AgentSkill agentSkill = SkillUtil.createFrom(loaded.getValue(), resources, SOURCE);

            result.add(agentSkill);
        }
        return result;
    }

    public List<McpClientWrapper> findActivatedInstallMcp(
            String sessionId,
            AuditAuthenticationToken token
    ) {
        return null;
    }
}
