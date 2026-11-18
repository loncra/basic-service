package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.util.SkillUtil;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.SkillUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.*;
import io.github.loncra.basic.service.ai.server.dao.hub.AiUserPluginInstallDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.*;
import io.github.loncra.basic.service.ai.server.resolver.channel.AgentChannelResolverFactory;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    private final AiUserPluginInstallSpecificService aiUserPluginInstallSpecificService;

    public AiUserPluginInstallEntity findUserInstallPluginByPrincipal(
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

    public Map<Long, List<String>> groupingBySpecific(List<AiUserPluginInstallEntity> installs) {
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

    public void replaceSpecific(
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

    public void clearSpecific(Long installId) {
        if (Objects.isNull(installId)) {
            return;
        }
        aiUserPluginInstallSpecificService.lambdaUpdate()
                .eq(AiUserPluginInstallSpecificEntity::getAiUserPluginInstallId, installId)
                .remove();
    }

    /*public List<AgentSkill> findActivatedInstallAgentSkills(
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
    }*/
}
