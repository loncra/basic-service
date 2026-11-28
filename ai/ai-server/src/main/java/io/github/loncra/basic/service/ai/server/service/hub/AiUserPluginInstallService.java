package io.github.loncra.basic.service.ai.server.service.hub;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.dao.hub.AiUserPluginInstallDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.plugin.UserPluginMetadata;
import io.github.loncra.basic.service.commons.enumerate.OwnershipEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
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

    /**
     * 按归属查未禁用的安装行：个人 = principal 维度（仅本人）；企业 = tenantId 维度（对全体成员可见）
     */
    public List<AiUserPluginInstallEntity> findRows(
            AuditAuthenticationToken token,
            OwnershipEnum ownership,
            PluginTargetTypeEnum targetType
    ) {
        LambdaQueryChainWrapper<AiUserPluginInstallEntity> query = lambdaQuery()
                .ne(AiUserPluginInstallEntity::getStatus, PluginInstallStatusEnum.DISABLED)
                .eq(AiUserPluginInstallEntity::getScope, ownership);
        if (OwnershipEnum.ENTERPRISE.equals(ownership)) {
            query.eq(AiUserPluginInstallEntity::getTenantId, TenantContextHolder.get().getId());
        } else {
            query.eq(AiUserPluginInstallEntity::getPrincipal, token.getName());
        }
        if (Objects.nonNull(targetType)) {
            query.eq(AiUserPluginInstallEntity::getTargetType, targetType);
        }
        return query.list();
    }

    /**
     * 按租户查找同包唯一企业安装行（安装幂等更新与卸载定位用）
     */
    public AiUserPluginInstallEntity findEnterpriseInstall(
            String tenantId,
            PluginTargetTypeEnum targetType,
            Long packageId
    ) {
        return lambdaQuery()
                .eq(AiUserPluginInstallEntity::getTenantId, tenantId)
                .eq(AiUserPluginInstallEntity::getScope, OwnershipEnum.ENTERPRISE)
                .eq(AiUserPluginInstallEntity::getTargetType, targetType)
                .eq(AiUserPluginInstallEntity::getPackageId, packageId)
                .one();
    }

    public AiUserPluginInstallEntity findUserInstallPluginByPrincipal(
            String principal,
            PluginTargetTypeEnum targetType,
            Long packageId,
            OwnershipEnum scope
    ) {
        return lambdaQuery()
                .eq(AiUserPluginInstallEntity::getPrincipal, principal)
                .eq(AiUserPluginInstallEntity::getTargetType, targetType)
                .eq(AiUserPluginInstallEntity::getPackageId, packageId)
                .eq(UserPluginMetadata::getScope, scope.getValue())
                .one();
    }

    public List<AiUserPluginInstallEntity> findActivatedInstalls(
            AuditAuthenticationToken token
    ) {
        return findActivatedInstalls(null, token);
    }

    public List<AiUserPluginInstallEntity> findActivatedInstalls(
            PluginTargetTypeEnum targetType,
            AuditAuthenticationToken token
    ) {
        Map<String, AiUserPluginInstallEntity> effective = new LinkedHashMap<>();

        if (ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE.equals(token.getType())) {
            LambdaQueryChainWrapper<AiUserPluginInstallEntity> query = lambdaQuery()
                    .eq(AiUserPluginInstallEntity::getStatus, PluginInstallStatusEnum.ACTIVATED)
                    .eq(AiUserPluginInstallEntity::getTenantId, TenantContextHolder.get().getId())
                    .eq(AiUserPluginInstallEntity::getScope, OwnershipEnum.ENTERPRISE);
            if (Objects.nonNull(targetType)) {
                query.eq(AiUserPluginInstallEntity::getTargetType, targetType);
            }
            query.list()
                    .forEach(s -> effective.put(s.getPackageId().toString(), s));
        }

        LambdaQueryChainWrapper<AiUserPluginInstallEntity> query = lambdaQuery()
                .ne(AiUserPluginInstallEntity::getStatus, PluginInstallStatusEnum.DISABLED)
                .eq(AiUserPluginInstallEntity::getScope, OwnershipEnum.USER)
                .eq(AiUserPluginInstallEntity::getPrincipal, token.getName());
        if (Objects.nonNull(targetType)) {
            query.eq(AiUserPluginInstallEntity::getTargetType, targetType);
        }
        query.list()
                .forEach(s -> effective.putIfAbsent(s.getPackageId().toString(), s));

        List<AiUserPluginInstallEntity> installs = effective.values().stream()
                .toList();

        return installs.stream()
                .filter(s -> PluginInstallWorkspaceScopeEnum.ALL.equals(s.getWorkspaceScope()))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
