package io.github.loncra.basic.service.auth.server.service.organization;

import io.github.loncra.basic.service.auth.server.dao.organization.OrganizationMemberDao;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationMemberEntity;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberStatusEnum;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * tb_organization_member 的业务逻辑
 *
 * <p>Table: tb_organization_member - 企业成员表</p>
 *
 * @author maurice.chen
 * @see OrganizationMemberEntity
 */
@Service
@RequiredArgsConstructor
public class OrganizationMemberService extends BasicService<OrganizationMemberDao, OrganizationMemberEntity> {

    public OrganizationMemberEntity getActiveMember(
            Long organizationId,
            String principal
    ) {
        return lambdaQuery()
                .eq(OrganizationMemberEntity::getOrganizationId, organizationId)
                .eq(OrganizationMemberEntity::getPrincipal, principal)
                .eq(OrganizationMemberEntity::getStatus, OrganizationMemberStatusEnum.ACTIVE)
                .one();
    }
}
