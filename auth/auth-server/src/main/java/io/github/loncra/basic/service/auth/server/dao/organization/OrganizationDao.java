package io.github.loncra.basic.service.auth.server.dao.organization;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_organization 的数据访问
 *
 * <p>Table: tb_organization - 企业表</p>
 *
 * @author maurice.chen
 * @see OrganizationEntity
 */
@Mapper
@Repository
public interface OrganizationDao extends BaseMapper<OrganizationEntity> {

}
