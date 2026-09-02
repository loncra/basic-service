package io.github.loncra.basic.service.auth.server.service.organization;

import io.github.loncra.basic.service.auth.server.dao.organization.OrganizationDao;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.organization.OrganizationMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * tb_organization 的业务逻辑
 *
 * <p>Table: tb_organization - 企业表</p>
 *
 * @author maurice.chen
 * @see OrganizationEntity
 */
@Service
@RequiredArgsConstructor
public class OrganizationService extends BasicService<OrganizationDao, OrganizationEntity> {

    private final OrganizationMemberService organizationMemberService;

    private final PersonalUserService personalUserService;

    private final AccessTokenContextRepository accessTokenContextRepository;

    public OrganizationEntity getAvailableOrganization(
            Long organizationId,
            String principal
    ) {
        if (Objects.isNull(organizationId)) {
            return null;
        }

        OrganizationEntity organization = get(organizationId);
        if (Objects.isNull(organization) || !YesOrNo.Yes.equals(organization.getEnabled())) {
            return null;
        }

        OrganizationMemberEntity member = organizationMemberService.getActiveMember(organizationId, principal);
        return Objects.nonNull(member) ? organization : null;
    }

    public void applyActiveOrganizationMetadata(
            PersonalUserEntity user,
            AuditAuthenticationSuccessDetails details
    ) {
        OrganizationEntity organization = getAvailableOrganization(
                user.getLastActiveOrganizationId(),
                user.getSystemName()
        );
        if (Objects.isNull(organization)) {
            details.getMetadata().put(TenantEntity.TENANT_ID_FIELD, user.getTenantId());
            details.getMetadata().remove(PrincipalDetailsConstants.ORGANIZATION_ID_KEY);
            return;
        }

        details.getMetadata().put(TenantEntity.TENANT_ID_FIELD, organization.getId().toString());
        details.getMetadata().put(PrincipalDetailsConstants.ORGANIZATION_ID_KEY, organization.getId());
    }

    public void switchOrganization(
            AuditAuthenticationToken token,
            Long organizationId
    ) {
        Assert.isTrue(
                ResourceSourceEnum.PERSONAL_SOURCE_VALUE.equals(token.getType()),
                "仅个人用户支持切换个人或企业空间"
        );

        PersonalUserEntity user = personalUserService.getByIdentity(
                Objects.toString(token.getSecurityPrincipal().getId())
        );
        Assert.notNull(user, "找不到当前个人用户");

        OrganizationEntity organization = getAvailableOrganization(organizationId, token.getName());
        if (Objects.nonNull(organizationId)) {
            Assert.notNull(organization, "当前用户不是该企业的有效成员，或企业已被禁用");
        }

        personalUserService.lambdaUpdate()
                .set(PersonalUserEntity::getLastActiveOrganizationId, organizationId)
                .eq(PersonalUserEntity::getId, user.getId())
                .update();
        user.setLastActiveOrganizationId(organizationId);

        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        applyActiveOrganizationMetadata(user, details);
        accessTokenContextRepository.saveAuthentication(token);
    }
}
