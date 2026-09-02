package io.github.loncra.basic.service.auth.server.dao.organization;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationInvitationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_organization_invitation 的数据访问
 *
 * <p>Table: tb_organization_invitation - 企业邀请表</p>
 *
 * @author maurice.chen
 * @see OrganizationInvitationEntity
 */
@Mapper
@Repository
public interface OrganizationInvitationDao extends BaseMapper<OrganizationInvitationEntity> {

}
