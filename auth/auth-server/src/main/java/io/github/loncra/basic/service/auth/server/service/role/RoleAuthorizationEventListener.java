package io.github.loncra.basic.service.auth.server.service.role;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.BasicSystemRole;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.resolver.SystemUserAuthorizationResolver;
import io.github.loncra.basic.service.auth.server.service.RedissonCacheAuthorizationService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.AbstractPluginResourceService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.disconvery.PluginNacosEventSourceListener;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;
import java.util.stream.Collectors;

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

    private final RoleService roleService;

    public List<AbstractBasicSystemUser> getSystemUsers(
            List<BasicSystemRole> roles
    ) {
        List<ResourceSourceEnum> sources = roles
                .stream()
                .flatMap(g -> g.getSources().stream())
                .toList();

        Set<Long> groupIds = roles.stream()
                .map(BasicSystemRole::getId)
                .collect(Collectors.toSet());
        List<AbstractBasicSystemUser> result = new LinkedList<>();
        for (ResourceSourceEnum source : sources) {
            SystemUserAuthorizationResolver<AbstractBasicSystemUser> userAuthorizationResolver = redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(source.getValue(), false);

            List<AbstractBasicSystemUser> systemUsers = new LinkedList<>(userAuthorizationResolver.findByRoleId(groupIds));
            if (CollectionUtils.isEmpty(systemUsers)) {
                continue;
            }
            result.addAll(systemUsers);
        }

        return result;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUpdated(UpdatedEvent event) {
        List<AbstractBasicSystemUser> basicSystemUsers = getSystemUsers(List.of(event.after()));
        for (AbstractBasicSystemUser basicSystemUser : basicSystemUsers) {
            syncUserRoleResourceId(basicSystemUser);
        }
        redissonCacheAuthorizationService.deleteAuthorizationCache(event.before().getSources());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDelete(DeleteEvent event) {
        List<AbstractBasicSystemUser> basicSystemUsers = getSystemUsers(List.of(event.entity()));
        for (AbstractBasicSystemUser basicSystemUser : basicSystemUsers) {
            basicSystemUser.getRoleIds().removeIf(id -> event.entity().getId().equals(id));
            syncUserRoleResourceId(basicSystemUser);
        }
        redissonCacheAuthorizationService.deleteAuthorizationCache(event.entity().getSources());
    }

    private void syncUserRoleResourceId(
            AbstractBasicSystemUser basicSystemUser
    ) {
        Map<Long, Set<Long>> roleResourceIdsMap = new LinkedHashMap<>();
        Set<Long> resourceIds = new HashSet<>();
        for (Long roleId : basicSystemUser.getRoleIds()) {
            Set<Long> roleResourceIds = roleResourceIdsMap.computeIfAbsent(roleId, id -> Optional.of(roleService.get(id)).map(BasicSystemRole::getResourceIds).orElse(new HashSet<>()));
            resourceIds.addAll(roleResourceIds);
        }
        if (CollectionUtils.isEmpty(basicSystemUser.getResourceIds())) {
            basicSystemUser.setResourceIds(new LinkedHashSet<>());
        } else {
            basicSystemUser.getResourceIds().clear();
        }
        basicSystemUser.getResourceIds().addAll(resourceIds);
        SystemUserAuthorizationResolver<AbstractBasicSystemUser> userAuthorizationResolver = redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(basicSystemUser.getType().getValue(), false);
        userAuthorizationResolver.updateResources(basicSystemUser.getId(), basicSystemUser.getResourceIds());
    }

    @EventListener
    public void onSyncPlugin(AbstractPluginResourceService.SyncPluginResourceEvent event) {
        Set<ResourceSourceEnum> resourceSources = Arrays.stream(ResourceSourceEnum.values())
                .filter(s -> Objects.nonNull(s.getAdminAuthority()))
                .collect(Collectors.toSet());
        for (ResourceSourceEnum source : resourceSources) {

            if (log.isDebugEnabled()) {
                log.debug("【同步角色资源权限】更新 {} 服务资源到 {} 角色中.", event.dto().getApplicationNames(), source.getAdminAuthority().getName());
            }

            RoleEntity before = roleService.getByAuthority(source.getAdminAuthority().getId());

            if (Objects.isNull(before)) {
                before = new RoleEntity();
                before.setName(source.getAdminAuthority().getName());
                before.setAuthority(source.getAdminAuthority().getId());
                before.setSources(Set.of(source));
                before.setRemovable(YesOrNo.No);
                before.setModifiable(YesOrNo.No);
                before.setEnabled(YesOrNo.Yes);
                roleService.insert(before);
            }

            if (CollectionUtils.isEmpty(before.getResourceIds())) {
                before.setResourceIds(new LinkedHashSet<>());
            }

            Set<Long> ids = event
                    .dto()
                    .getResources()
                    .stream()
                    .filter(r -> r.getSources().stream().anyMatch(source::equals))
                    .map(ResourceEntity::getId)
                    .collect(Collectors.toSet());

            RoleEntity after = CastUtils.of(before, RoleEntity.class);

            after.getResourceIds().addAll(ids);
            after.getResourceIds().removeIf(id -> event.dto().getDeleteIds().contains(id));

            String resourceIds = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(after.getResourceIds()));
            roleService.lambdaUpdate()
                    .set(RoleEntity::getResourceIds, resourceIds)
                    .eq(RoleEntity::getId, after.getId())
                    .update();

            if (log.isDebugEnabled()) {
                log.debug(
                        "【同步角色资源权限】完成更新 {} 服务 {} 条资源到 {} 角色，删除 {} 条作废的资源.",
                        event.dto().getApplicationNames(),
                        event.dto().getResources().size(),
                        source.getAdminAuthority().getName(),
                        event.dto().getDeleteIds().size()
                );
            }

            syncAdminUser(source, after);

            onUpdated(new UpdatedEvent(before, after));
        }
    }

    private void syncAdminUser(
            ResourceSourceEnum source,
            RoleEntity after
    ) {
        if (StringUtils.isEmpty(source.getAdminAuthority().getValue())) {
            return ;
        }

        SystemUserAuthorizationResolver<AbstractBasicSystemUser> userAuthorizationResolver = redissonCacheAuthorizationService.getSystemUserAuthorizationResolver(source.getValue(), false);
        if (Objects.isNull(userAuthorizationResolver)) {
            return ;
        }
        AbstractBasicSystemUser user = userAuthorizationResolver.getByIdentity(source.getAdminAuthority().getValue());
        if  (Objects.isNull(user)) {
            return ;
        }
        userAuthorizationResolver.updateRole(user.getId().toString(), Set.of(after.getId()));
    }

    @EventListener
    public void onDisabledPlugin(PluginNacosEventSourceListener.DisabledPluginResourceEvent event) {
        Set<ResourceSourceEnum> resourceSources = Arrays.stream(ResourceSourceEnum.values())
                .filter(s -> Objects.nonNull(s.getAdminAuthority()))
                .collect(Collectors.toSet());

        for (ResourceSourceEnum source : resourceSources) {
            if (log.isDebugEnabled()) {
                log.debug("【禁用角色资源权限】禁用 {} 服务在 {} 角色中的资源.", event.dto().getEvent().getServiceName(), source.getAdminAuthority().getName());
            }

            RoleEntity role = roleService.getByAuthority(source.getAdminAuthority().getId());

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

        redissonCacheAuthorizationService.deleteAuthorizationCache(resourceSources);
    }

    public record UpdatedEvent(RoleEntity before, RoleEntity after) {}

    public record DeleteEvent(RoleEntity entity) {}


}
