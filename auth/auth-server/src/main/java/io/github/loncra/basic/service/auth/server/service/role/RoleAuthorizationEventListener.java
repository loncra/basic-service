package io.github.loncra.basic.service.auth.server.service.role;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.service.RedissonCacheAuthorizationService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.AbstractPluginResourceService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.disconvery.PluginNacosEventSourceListener;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;

/**
 * 角色授权时间监听器
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleAuthorizationEventListener {

    private final RedissonCacheAuthorizationService<AbstractBasicSystemUser> redissonCacheAuthorizationService;

    private final AuthAppConfig authAppConfig;

    private final RoleService roleService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUpdated(UpdatedEvent event) {
        redissonCacheAuthorizationService.postUpdateRole(event.before());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDelete(DeleteEvent event) {
        redissonCacheAuthorizationService.postDeleteRole(event.entity());
    }

    @EventListener
    public void onSyncPlugin(AbstractPluginResourceService.SyncPluginResourceEvent event) {
        List<ResourceSourceEnum> sources = event.dto().getResources()
                .stream()
                .filter(r -> r.getApplicationName().equals(event.dto().getServiceName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .toList();

        for (IdNameValueMetadata<String, List<ResourceSourceEnum>> metadata : authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities()) {

            /*if (log.isDebugEnabled()) {
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

            if (CollectionUtils.isEmpty(role.getResourceIds())) {
                role.setResourceIds(new LinkedList<>());
            }

            // 删除不存在的资源
            List<ResourceMetadata> removes = role
                    .getResourceIds()
                    .stream()
                    .flatMap()
                    .filter(s -> s.getApplicationName().equals(dto.getServiceName()))
                    .filter(s -> newResources.stream().noneMatch(n -> n.getId().equals(s.getApplicationName())))
                    .toList();
            role.getResourceIds()
                    .removeAll(removes);

            // 覆盖当前应用的资源
            newResources
                    .stream()
                    .filter(s -> !role.getResourceIds().contains(s))
                    .forEach(s -> role.getResourceIds().add(s));
            String resourceMap = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(role.getResourceIds()));
            roleService.lambdaUpdate()
                    .set(RoleEntity::getResourceIds, resourceMap)
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

            updateUserResource(role, metadata.getValue());*/
        }

        redissonCacheAuthorizationService.deleteAuthorizationCache(sources);
    }

    @EventListener
    public void onDisabledPlugin(PluginNacosEventSourceListener.DisabledPluginResourceEvent event) {
        List<ResourceSourceEnum> sources = event.dto().getResources()
                .stream()
                .filter(r -> r.getApplicationName().equals(event.dto().getEvent().getServiceName()))
                .flatMap(r -> r.getSources().stream())
                .distinct()
                .toList();

        for (IdNameValueMetadata<String, List<ResourceSourceEnum>> metadata : authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities()) {
            if (log.isDebugEnabled()) {
                log.debug("【禁用角色资源权限】禁用 {} 服务在 {} 角色中的资源.", event.dto().getEvent().getServiceName(), metadata.getName());
            }

            RoleEntity role = roleService.getByAuthority(metadata.getId());

            // 如果配置了管理员组 线删除同步一次管理员資源
            if (Objects.isNull(role)) {
                return;
            }

            List<Long> disabledResourceIds = event.dto()
                    .getResources()
                    .stream()
                    .filter(r -> r.getSources().stream().anyMatch(s -> role.getSources().contains(s)))
                    .filter(r -> r.getApplicationName().equals(event.dto().getEvent().getServiceName()))
                    .map(ResourceEntity::getId)
                    .toList();

            role.getResourceIds()
                    .removeIf(disabledResourceIds::contains);
            String resourceString = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(role.getResourceIds()));
            roleService
                    .lambdaUpdate()
                    .set(RoleEntity::getResourceIds, resourceString)
                    .eq(RoleEntity::getId, role.getId())
                    .update();
        }

        redissonCacheAuthorizationService.deleteAuthorizationCache(sources);
    }

    public record UpdatedEvent(RoleEntity before, RoleEntity after) {}

    public record DeleteEvent(RoleEntity entity) {}


}
