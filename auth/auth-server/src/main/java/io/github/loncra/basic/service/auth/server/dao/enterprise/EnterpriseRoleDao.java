package io.github.loncra.basic.service.auth.server.dao.enterprise;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_enterprise_role 的数据访问
 *
 * <p>Table: tb_enterprise_role - 企业用户组表</p>
 *
 * @see EnterpriseRoleEntity
 *
 * @author maurice.chen
 *
 * @since 2026-09-05 06:30:36
 */
@Mapper
@Repository
public interface EnterpriseRoleDao extends BaseMapper<EnterpriseRoleEntity> {

}
