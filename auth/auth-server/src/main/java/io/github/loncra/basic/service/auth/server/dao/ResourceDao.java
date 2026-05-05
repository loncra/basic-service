package io.github.loncra.basic.service.auth.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_resource 的数据访问
 *
 * <p>Table: tb_resource - 资源表</p>
 *
 * @see ResourceEntity
 *
 * @author maurice.chen
 *
 * @since 2026-05-04 09:53:45
 */
@Mapper
@Repository
public interface ResourceDao extends BaseMapper<ResourceEntity> {

}
