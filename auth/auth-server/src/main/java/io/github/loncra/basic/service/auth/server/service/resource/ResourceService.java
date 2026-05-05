package io.github.loncra.basic.service.auth.server.service.resource;

import io.github.loncra.basic.service.auth.server.dao.ResourceDao;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public ResourceEntity getByCode(String code) {
        return lambdaQuery().eq(ResourceEntity::getCode, code).one();
    }
}
