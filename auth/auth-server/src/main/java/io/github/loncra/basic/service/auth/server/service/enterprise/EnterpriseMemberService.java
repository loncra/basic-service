package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseMemberDao;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberStatusEnum;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * tb_enterprise_member 的业务逻辑
 *
 * <p>Table: tb_enterprise_member - 企业成员表</p>
 *
 * @author maurice.chen
 * @see EnterpriseMemberEntity
 */
@Service
@RequiredArgsConstructor
public class EnterpriseMemberService extends BasicService<EnterpriseMemberDao, EnterpriseMemberEntity> {

    public EnterpriseMemberEntity getActiveMember(
            Long organizationId,
            String principal
    ) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getOrganizationId, organizationId)
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .eq(EnterpriseMemberEntity::getStatus, OrganizationMemberStatusEnum.ACTIVE)
                .one();
    }

    public EnterpriseMemberEntity getMember(
            Long organizationId,
            String principal
    ) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getOrganizationId, organizationId)
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .one();
    }

    public List<EnterpriseMemberEntity> findActiveByPrincipal(String principal) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getPrincipal, principal)
                .eq(EnterpriseMemberEntity::getStatus, OrganizationMemberStatusEnum.ACTIVE)
                .list();
    }

    public List<EnterpriseMemberEntity> findByOrganizationId(Long organizationId) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getOrganizationId, organizationId)
                .list();
    }

    public EnterpriseMemberEntity requireManager(
            Long organizationId,
            String principal
    ) {
        EnterpriseMemberEntity member = getActiveMember(organizationId, principal);
        Assert.notNull(member, "当前用户不是该企业的有效成员");
        Assert.isTrue(
                OrganizationMemberRoleEnum.MANAGER_ROLES.contains(member.getRole()),
                "当前用户没有企业管理权限"
        );
        return member;
    }

    public long countActiveOwners(Long organizationId) {
        return lambdaQuery()
                .eq(EnterpriseMemberEntity::getOrganizationId, organizationId)
                .eq(EnterpriseMemberEntity::getRole, OrganizationMemberRoleEnum.OWNER)
                .eq(EnterpriseMemberEntity::getStatus, OrganizationMemberStatusEnum.ACTIVE)
                .count();
    }
}
