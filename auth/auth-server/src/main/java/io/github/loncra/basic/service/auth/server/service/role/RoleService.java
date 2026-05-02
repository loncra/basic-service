package io.github.loncra.basic.service.auth.server.service.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.dao.RoleDao;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.service.RedissonCacheAuthorizationService;
import io.github.loncra.basic.service.auth.server.service.plugin.PluginResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * tb_role 的业务逻辑
 *
 * <p>Table: tb_role - 角色表</p>
 *
 * @author maurice.chen
 * @see RoleEntity
 * @since 2021-11-25 02:42:57
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService extends BasicService<RoleDao, RoleEntity> implements InitializingBean {

    private final AuthAppConfig authAppConfig;

    @Getter
    private final RedissonCacheAuthorizationService<AbstractBasicSystemUser> redissonCacheAuthorizationService;

    @Getter
    private final PluginResourceService pluginResourceService;

    @Override
    public int save(RoleEntity entity) {

        List<ResourceMetadata> groupResource = getGroupResource(entity);

        List<String> noneMatchSources = groupResource.stream()
                .filter(r -> r.getSources().stream().noneMatch(s -> entity.getSources().contains(s)))
                .distinct()
                .flatMap(r -> r.getSources().stream().filter(s -> !entity.getSources().contains(s)))
                .map(ResourceSourceEnum::getName)
                .toList();

        if (!noneMatchSources.isEmpty()) {
            List<String> sourceNames = entity.getSources()
                    .stream()
                    .map(ResourceSourceEnum::getName)
                    .distinct()
                    .toList();

            throw new ServiceException("组来源 " + sourceNames + " 不能保存属于 " + noneMatchSources + " 的资源");
        }

        return super.save(entity);
    }

    /**
     * 获取组资源结合
     *
     * @param group 组信息
     *
     * @return 资源元数据信息
     */
    public List<ResourceMetadata> getGroupResource(RoleEntity group) {
        List<ResourceMetadata> result = new LinkedList<>();

        if (CollectionUtils.isEmpty(group.getResources())) {
            return result;
        }
        Map<String, List<String>> resourceMap = group.getResources()
                .stream()
                .collect(Collectors.groupingBy(IdResourceAuthorityMetadata::getApplicationName, Collectors.mapping(IdResourceAuthorityMetadata::getId, Collectors.toList())));
        for (Map.Entry<String, List<String>> entry : resourceMap.entrySet()) {
            List<ResourceMetadata> resources = pluginResourceService.getResources(entry.getKey(), group.getSources()
                    .toArray(new ResourceSourceEnum[0]));
            List<ResourceMetadata> findResources = resources
                    .stream()
                    .filter(r -> entry.getValue().contains(r.getId()))
                    .toList();

            result.addAll(findResources);
        }

        return result;
    }

    @Override
    public int insert(RoleEntity entity) {

        LambdaQueryWrapper<RoleEntity> wrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getName, entity.getName());
        SystemException.isTrue(!exist(wrapper), "角色 [" + entity.getName() + "] 已存在");

        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(RoleEntity entity) {
        RoleEntity exist = get(entity.getId());
        Assert.isTrue(YesOrNo.Yes.equals(exist.getModifiable()), "角色 [" + entity.getName() + "] 被设置为不可修改角色，无法执行操作。");

        int result = super.updateById(entity);

        redissonCacheAuthorizationService.postUpdateRole(exist);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Collection<? extends Serializable> ids,
            boolean errorThrow
    ) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除用户角色 ID 为 [" + ids + "] 的数据发生异常";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Serializable id) {
        return deleteByEntity(get(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(
            Collection<RoleEntity> entities,
            boolean errorThrow
    ) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除" + entities.size() + " 条用户角色数据发生异常";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(RoleEntity entity) {
        Assert.isTrue(
                authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities().stream().noneMatch(r -> r.getId().equals(entity.getAuthority())),
                "管理员角色不能删除"
        );
        Assert.isTrue(YesOrNo.Yes.equals(entity.getRemovable()), "角色 [" + entity.getName() + "] 被设置为不可删除角色，无法执行操作");
        redissonCacheAuthorizationService.postDeleteRole(entity);
        return super.deleteByEntity(entity);
    }

    public RoleEntity getByAuthority(String value) {
        return lambdaQuery().eq(RoleEntity::getAuthority, value)
                .one();
    }

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afterPropertiesSet() throws Exception {

        for (IdNameValueMetadata<String, List<ResourceSourceEnum>> metadata : authAppConfig.getAutoAssociateAllPermissionsRoleAuthorities()) {
            RoleEntity entity = getByAuthority(metadata.getId());
            if (Objects.nonNull(entity)) {
                continue;
            }
            createSystemGroup(metadata.getName(), metadata.getId(), metadata.getValue());
        }
    }

    private void createSystemGroup(
            String name,
            String authority,
            List<ResourceSourceEnum> sources
    ) {
        RoleEntity group = new RoleEntity();

        group.setRemovable(YesOrNo.No);
        group.setModifiable(YesOrNo.No);
        group.setName(name);
        group.setAuthority(authority);
        group.setSources(sources);

        super.insert(group);
    }

    public List<ResourceMetadata> getSystemUserResource(
            AuditAuthenticationToken token,
            List<String> types,
            List<ResourceSourceEnum> sourceContains
    ) {
        AbstractBasicSystemUser user = redissonCacheAuthorizationService
                .getSystemUserAuthorizationResolver(token.getType(), true)
                .load(token);
        List<String> resourceIds = user.getResources()
                .stream()
                .map(IdResourceAuthorityMetadata::getId)
                .toList();
        List<ResourceMetadata> userResource = pluginResourceService.getResources()
                .stream()
                .filter(r -> resourceIds.contains(r.getId()))
                .toList();

        Stream<ResourceMetadata> stream = userResource
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains));

        if (Objects.nonNull(token.getType())) {
            stream = stream.filter(r -> types.contains(r.getType()));
        }

        return stream.collect(Collectors.toList());
    }
}
