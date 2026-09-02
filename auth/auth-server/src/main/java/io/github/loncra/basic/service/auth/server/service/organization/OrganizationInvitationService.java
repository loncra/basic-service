package io.github.loncra.basic.service.auth.server.service.organization;

import io.github.loncra.basic.service.auth.server.dao.organization.OrganizationInvitationDao;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationInvitationEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * tb_organization_invitation 的业务逻辑
 *
 * <p>Table: tb_organization_invitation - 企业邀请表</p>
 *
 * @author maurice.chen
 * @see OrganizationInvitationEntity
 */
@Service
@RequiredArgsConstructor
public class OrganizationInvitationService extends BasicService<OrganizationInvitationDao, OrganizationInvitationEntity> {

}
