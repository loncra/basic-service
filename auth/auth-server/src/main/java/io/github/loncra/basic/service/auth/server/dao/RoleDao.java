
package io.github.loncra.basic.service.auth.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_role 用户角色数据访问
 *
 * <p>Table: tb_role - 用户角色</p>
 *
 * @author maurice
 * @see RoleEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface RoleDao extends BaseMapper<RoleEntity> {

}
