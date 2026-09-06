package io.github.loncra.basic.service.auth.server.service.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.dao.RoleDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.PluginResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
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
public class RoleService extends BasicService<RoleDao, RoleEntity> {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Getter
    private final PluginResourceService pluginResourceService;

    @Override
    public int save(RoleEntity entity) {

        List<ResourceEntity> groupResource = getGroupResource(entity);

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
    public List<ResourceEntity> getGroupResource(RoleEntity group) {
        List<ResourceEntity> result = new LinkedList<>();

        if (CollectionUtils.isEmpty(group.getResourceIds())) {
            return result;
        }

        return pluginResourceService.getResourcesStream(group.getResourceIds(), group.getSources().toArray(new ResourceSourceEnum[0]));
    }

    public List<ResourceEntity> getSystemUserResource(
            Set<Long> resourceIds,
            List<ResourceTypeEnum> types,
            List<ResourceSourceEnum> sourceContains

    ) {
        Stream<ResourceEntity> stream = pluginResourceService
                .getResources()
                .stream()
                .filter(r -> resourceIds.contains(r.getId()))
                .filter(r -> r.getSources().stream().anyMatch(sourceContains::contains));
        if (CollectionUtils.isNotEmpty(types)) {
            stream = stream.filter(r -> types.contains(r.getType()));
        }

        return stream.toList();
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
        applicationEventPublisher.publishEvent(new RoleAuthorizationEventListener.UpdatedEvent(exist, entity));

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
        Set<String> authorities = entity.getSources()
                .stream()
                .filter(s -> Objects.nonNull(s.getAdminAuthority()))
                .filter(s -> entity.getSources().contains(s))
                .map(ResourceSourceEnum::getAdminAuthority)
                .map(IdEntity::getId).collect(Collectors.toSet());

        Assert.isTrue(
                !authorities.contains(entity.getAuthority()),
                "管理员角色不能删除"
        );
        Assert.isTrue(YesOrNo.Yes.equals(entity.getRemovable()), "角色 [" + entity.getName() + "] 被设置为不可删除角色，无法执行操作");
        //redissonCacheAuthorizationService.postDeleteRole(entity);
        applicationEventPublisher.publishEvent(new RoleAuthorizationEventListener.DeleteEvent(entity));

        findByParentId(entity.getId()).forEach(this::deleteByEntity);

        return super.deleteByEntity(entity);
    }

    public List<RoleEntity> findByParentId(Long id) {
        return lambdaQuery().eq(RoleEntity::getParentId, id).list();
    }

    public RoleEntity getByAuthority(String value) {
        return lambdaQuery().eq(RoleEntity::getAuthority, value)
                .one();
    }
}
