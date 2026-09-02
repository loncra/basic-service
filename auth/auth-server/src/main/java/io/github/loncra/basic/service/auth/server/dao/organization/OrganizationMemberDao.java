package io.github.loncra.basic.service.auth.server.dao.organization;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationMemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_organization_member 的数据访问
 *
 * <p>Table: tb_organization_member - 企业成员表</p>
 *
 * @author maurice.chen
 * @see OrganizationMemberEntity
 */
@Mapper
@Repository
public interface OrganizationMemberDao extends BaseMapper<OrganizationMemberEntity> {

}
