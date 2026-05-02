package io.github.loncra.basic.service.auth.server.service.role;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.domain.dto.AbstractSyncPluginResourceDto;
import io.github.loncra.basic.service.auth.server.domain.dto.DisabledApplicationResourceDto;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.resolver.PluginResourceResolver;
import io.github.loncra.basic.service.auth.server.service.RedissonCacheAuthorizationService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 角色插件资源拦截器实现，主要是自动同步一些默认角色的权限信息。
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolePluginResourceResolver implements PluginResourceResolver {

    private final RedissonCacheAuthorizationService<AbstractBasicSystemUser> redissonCacheAuthorizationService;

    private final AuthAppConfig authAppConfig;

    private final RoleService roleService;

    @Override
    public void postSyncPlugin(AbstractSyncPluginResourceDto dto) {

        List<ResourceSourceEnum> sources = dto.getResources()
                .stream()
                .filter(r -> r.getApplicationName().equals(dto.getServiceName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .toList();

        for (IdNameValueMetadata<String, List<ResourceSourceEnum>> metadata : authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities()) {

            if (log.isDebugEnabled()) {
                log.debug("【同步角色资源权限】更新 {} 服务资源到 {} 角色中.", dto.getServiceName(), metadata.getName());
            }

            RoleEntity role = roleService.getByAuthority(metadata.getId());

            // 如果配置了管理员组 线删除同步一次管理员資源
            if (Objects.isNull(role)) {
                return;
            }

            List<ResourceMetadata> newResources = dto
                    .getResources()
                    .stream()
                    .filter(r -> r.getSources().stream().anyMatch(s -> role.getSources().contains(s)))
                    .toList();

            if (CollectionUtils.isEmpty(role.getResources())) {
                role.setResources(new LinkedList<>());
            }

            // 删除不存在的资源
            List<IdResourceAuthorityMetadata> removes = role
                    .getResources()
                    .stream()
                    .filter(s -> s.getApplicationName().equals(dto.getServiceName()))
                    .filter(s -> newResources.stream().noneMatch(n -> n.getId().equals(s.getApplicationName())))
                    .toList();
            role.getResources()
                    .removeAll(removes);

            // 覆盖当前应用的资源
            newResources
                    .stream()
                    .map(s -> CastUtils.of(s, IdResourceAuthorityMetadata.class))
                    .filter(s -> !role.getResources().contains(s))
                    .forEach(s -> role.getResources().add(s));
            String resourceMap = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(role.getResources()));
            roleService.lambdaUpdate()
                    .set(RoleEntity::getResources, resourceMap)
                    .eq(RoleEntity::getId, role.getId())
                    .update();

            if (log.isDebugEnabled()) {
                log.debug(
                        "【同步角色资源权限】完成更新 {} 服务 {} 条资源到 {} 角色.",
                        dto.getServiceName(),
                        dto.getResources().size(),
                        metadata.getName()
                );
            }

            updateUserResource(role, metadata.getValue());
        }

        redissonCacheAuthorizationService.deleteAuthorizationCache(sources);
    }

    private void updateUserResource(
            RoleEntity role,
            List<ResourceSourceEnum> sources
    ) {
        List<AbstractBasicSystemUser> users = sources
                .stream()
                .flatMap(s -> roleService.getRedissonCacheAuthorizationService().findByRoleAuthority(s.getValue(), role.getAuthority()).stream())
                .toList();
        for (AbstractBasicSystemUser user : users) {
            List<String> userResourceIds = user.getResources()
                    .stream()
                    .map(IdResourceAuthorityMetadata::getId)
                    .toList();
            List<IdResourceAuthorityMetadata> resourceAuthorities = role
                    .getResources()
                    .stream()
                    .filter(s -> !userResourceIds.contains(s.getId()))
                    .toList();
            user.getResources()
                    .addAll(resourceAuthorities);
            roleService
                    .getRedissonCacheAuthorizationService()
                    .updateResources(user.getType().getValue(), user.getId().toString(), user.getResources());
        }
    }

    @Override
    public void postDisabledApplicationResource(DisabledApplicationResourceDto dto) {

        List<ResourceSourceEnum> sources = dto.getResources()
                .stream()
                .filter(r -> r.getApplicationName().equals(dto.getEvent().getServiceName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .toList();

        for (IdNameValueMetadata<String, List<ResourceSourceEnum>> metadata : authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities()) {
            if (log.isDebugEnabled()) {
                log.debug("【禁用角色资源权限】禁用 {} 服务在 {} 角色中的资源.", dto.getEvent().getServiceName(), metadata.getName());
            }

            RoleEntity role = roleService.getByAuthority(metadata.getId());

            // 如果配置了管理员组 线删除同步一次管理员資源
            if (Objects.isNull(role)) {
                return;
            }

            List<String> disabledResourceIds = dto
                    .getResources()
                    .stream()
                    .filter(r -> r.getSources().stream().anyMatch(s -> role.getSources().contains(s)))
                    .filter(r -> r.getApplicationName().equals(dto.getEvent().getServiceName()))
                    .map(IdResourceAuthorityMetadata::getId)
                    .toList();

            role.getResources()
                    .removeIf(r -> disabledResourceIds.contains(r.getId()));
            String resourceMap = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(role.getResources()));
            roleService
                    .lambdaUpdate()
                    .set(RoleEntity::getResources, resourceMap)
                    .eq(RoleEntity::getId, role.getId())
                    .update();
            updateUserResource(role, metadata.getValue());
        }

        redissonCacheAuthorizationService.deleteAuthorizationCache(sources);
    }
}