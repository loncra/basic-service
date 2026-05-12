package io.github.loncra.basic.service.auth.server.service.resource;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceCategoryEnum;
import io.github.loncra.basic.service.auth.server.dao.ResourceDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.service.role.RoleAuthorizationEventListener;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * tb_resource 的业务逻辑
 *
 * <p>Table: tb_resource - 资源表</p>
 *
 * @see ResourceEntity
 *
 * @author maurice.chen
 *
 * @since 2026-05-04 09:53:45
 */
@Service
@RequiredArgsConstructor
public class ResourceService extends BasicService<ResourceDao, ResourceEntity> {

    public static final String DEFAULT_APPLICATION = "system";

    public static final String DEFAULT_VERSION = "1.0.0";

    public ResourceEntity getByCode(String code) {
        return lambdaQuery().eq(ResourceEntity::getCode, code).one();
    }

    @Override
    public int insert(ResourceEntity entity) {
        if (StringUtils.isEmpty(entity.getCode())) {
            entity.setCode(DigestUtils.md5DigestAsHex(String.valueOf(System.currentTimeMillis()).getBytes()));
        }
        if (StringUtils.isEmpty(entity.getApplicationName())) {
            entity.setApplicationName(DEFAULT_APPLICATION);
        }
        if (StringUtils.isEmpty(entity.getVersion())) {
            entity.setVersion(DEFAULT_VERSION);
        }
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Collection<? extends Serializable> ids,
            boolean errorThrow
    ) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除权限资源 ID 为 [" + ids + "] 的数据发生异常";
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
            Collection<ResourceEntity> entities,
            boolean errorThrow
    ) {
        int result = entities.stream().mapToInt(this::deleteByEntity).sum();
        if (result != entities.size() && errorThrow) {
            String msg = "删除" + entities.size() + " 条权限资源数据发生异常";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(ResourceEntity entity) {
        SystemException.isTrue(ResourceCategoryEnum.CUSTOMIZE.equals(entity.getCategory()), entity.getCategory().getName() + "资源不能删除");
        findByParentId(entity.getId()).forEach(this::deleteByEntity);

        return super.deleteByEntity(entity);
    }

    private List<ResourceEntity> findByParentId(Long id) {
        return lambdaQuery().eq(ResourceEntity::getParentId, id).list();
    }
}
