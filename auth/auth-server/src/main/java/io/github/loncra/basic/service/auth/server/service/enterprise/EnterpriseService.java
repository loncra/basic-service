package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseDao;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationInvitationStatusEnum;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberStatusEnum;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * tb_enterprise 的业务逻辑
 *
 * <p>Table: tb_enterprise - 企业表</p>
 *
 * @author maurice.chen
 * @see EnterpriseEntity
 */
@Service
@RequiredArgsConstructor
public class EnterpriseService extends BasicService<EnterpriseDao, EnterpriseEntity> {

    private final EnterpriseMemberService enterpriseMemberService;

    private final EnterpriseInvitationService enterpriseInvitationService;

    private final PersonalUserService personalUserService;

    private final AccessTokenContextRepository accessTokenContextRepository;

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(7);

    public EnterpriseEntity getAvailableOrganization(
            Long organizationId,
            String principal
    ) {
        if (Objects.isNull(organizationId)) {
            return null;
        }

        EnterpriseEntity organization = get(organizationId);
        if (Objects.isNull(organization) || !YesOrNo.Yes.equals(organization.getEnabled())) {
            return null;
        }

        EnterpriseMemberEntity member = enterpriseMemberService.getActiveMember(organizationId, principal);
        return Objects.nonNull(member) ? organization : null;
    }

    public void applyActiveOrganizationMetadata(
            PersonalUserEntity user,
            AuditAuthenticationSuccessDetails details
    ) {
        EnterpriseEntity organization = getAvailableOrganization(
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

        PersonalUserEntity user = personalUserService.getByIdentity(
                Objects.toString(token.getSecurityPrincipal().getId())
        );
        Assert.notNull(user, "找不到当前个人用户");

        EnterpriseEntity organization = getAvailableOrganization(organizationId, token.getName());
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

    @Transactional(rollbackFor = Exception.class)
    public EnterpriseEntity create(
            AuditAuthenticationToken token,
            String name,
            String remark
    ) {
        Assert.isTrue(StringUtils.isNotBlank(name), "企业名称不能为空");

        EnterpriseEntity organization = new EnterpriseEntity();
        organization.setName(name);
        organization.setOwnerPrincipal(token.getName());
        organization.setRemark(remark);
        insert(organization);

        EnterpriseMemberEntity owner = new EnterpriseMemberEntity();
        owner.setOrganizationId(organization.getId());
        owner.setPrincipal(token.getName());
        owner.setRole(OrganizationMemberRoleEnum.OWNER);
        owner.setStatus(OrganizationMemberStatusEnum.ACTIVE);
        enterpriseMemberService.insert(owner);

        switchOrganization(token, organization.getId());
        return organization;
    }

    public List<EnterpriseEntity> findByOrganizationIds(AuditAuthenticationToken token) {
        List<Long> organizationIds = enterpriseMemberService.findActiveByPrincipal(token.getName())
                .stream()
                .map(EnterpriseMemberEntity::getOrganizationId)
                .distinct()
                .toList();
        if (organizationIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .in(EnterpriseEntity::getId, organizationIds)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public EnterpriseInvitationEntity invite(
            AuditAuthenticationToken token,
            Long organizationId,
            String phoneNumber
    ) {
        requireEnabledOrganization(organizationId);
        enterpriseMemberService.requireManager(organizationId, token.getName());
        Assert.isTrue(
                Pattern.matches(SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION, phoneNumber),
                "被邀请手机号格式不正确"
        );

        PersonalUserEntity invitedUser = personalUserService.lambdaQuery()
                .eq(PersonalUserEntity::getPhoneNumber, phoneNumber)
                .one();
        if (Objects.nonNull(invitedUser)) {
            EnterpriseMemberEntity member = enterpriseMemberService.getMember(
                    organizationId,
                    invitedUser.getSystemName()
            );
            Assert.isTrue(
                    Objects.isNull(member) || OrganizationMemberStatusEnum.DISABLED.equals(member.getStatus()),
                    "该用户已在企业中或正在等待加入"
            );
        }

        EnterpriseInvitationEntity pending = enterpriseInvitationService.getPendingInvitation(
                organizationId,
                phoneNumber
        );
        if (Objects.nonNull(pending) && pending.getExpirationTime().isAfter(Instant.now())) {
            throw new IllegalArgumentException("该手机号已有待接受的企业邀请");
        }
        if (Objects.nonNull(pending)) {
            pending.setStatus(OrganizationInvitationStatusEnum.EXPIRED);
            enterpriseInvitationService.save(pending);
        }

        EnterpriseInvitationEntity invitation = new EnterpriseInvitationEntity();
        invitation.setOrganizationId(organizationId);
        invitation.setCode(StringUtils.remove(UUID.randomUUID().toString(), '-'));
        invitation.setPhoneNumber(phoneNumber);
        invitation.setInviterPrincipal(token.getName());
        invitation.setExpirationTime(Instant.now().plus(INVITATION_VALIDITY));
        enterpriseInvitationService.insert(invitation);
        return invitation;
    }

    @Transactional(rollbackFor = Exception.class)
    public void acceptInvitation(
            AuditAuthenticationToken token,
            String code
    ) {
        EnterpriseInvitationEntity invitation = enterpriseInvitationService.getByCode(code);
        Assert.notNull(invitation, "找不到企业邀请");
        Assert.isTrue(
                OrganizationInvitationStatusEnum.PENDING.equals(invitation.getStatus()),
                "企业邀请已失效"
        );
        Assert.isTrue(invitation.getExpirationTime().isAfter(Instant.now()), "企业邀请已过期");
        requireEnabledOrganization(invitation.getOrganizationId());

        PersonalUserEntity user = personalUserService.getByIdentity(
                Objects.toString(token.getSecurityPrincipal().getId())
        );
        Assert.notNull(user, "找不到当前个人用户");
        Assert.isTrue(
                Objects.equals(user.getPhoneNumber(), invitation.getPhoneNumber()),
                "当前用户手机号与企业邀请不一致"
        );

        EnterpriseMemberEntity member = enterpriseMemberService.getMember(
                invitation.getOrganizationId(),
                token.getName()
        );
        if (Objects.isNull(member)) {
            member = new EnterpriseMemberEntity();
            member.setOrganizationId(invitation.getOrganizationId());
            member.setPrincipal(token.getName());
            member.setRole(OrganizationMemberRoleEnum.MEMBER);
        }
        member.setStatus(OrganizationMemberStatusEnum.ACTIVE);
        enterpriseMemberService.save(member);

        invitation.setStatus(OrganizationInvitationStatusEnum.ACCEPTED);
        enterpriseInvitationService.save(invitation);
    }

    public List<EnterpriseMemberEntity> findMembers(
            AuditAuthenticationToken token,
            Long organizationId
    ) {
        Assert.notNull(
                getAvailableOrganization(organizationId, token.getName()),
                "当前用户不是该企业的有效成员，或企业已被禁用"
        );
        return enterpriseMemberService.findByOrganizationId(organizationId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMember(
            AuditAuthenticationToken token,
            Long organizationId,
            String principal
    ) {
        requireEnabledOrganization(organizationId);
        enterpriseMemberService.requireManager(organizationId, token.getName());
        Assert.isTrue(!Objects.equals(token.getName(), principal), "不能通过移除成员退出企业");

        EnterpriseMemberEntity member = enterpriseMemberService.getActiveMember(organizationId, principal);
        Assert.notNull(member, "找不到有效的企业成员");
        Assert.isTrue(
                !OrganizationMemberRoleEnum.OWNER.equals(member.getRole()),
                "不能移除企业主"
        );
        member.setStatus(OrganizationMemberStatusEnum.DISABLED);
        enterpriseMemberService.save(member);
        clearRemovedMemberContext(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leave(
            AuditAuthenticationToken token,
            Long organizationId
    ) {
        EnterpriseMemberEntity member = enterpriseMemberService.getActiveMember(
                organizationId,
                token.getName()
        );
        Assert.notNull(member, "当前用户不是该企业的有效成员");
        if (OrganizationMemberRoleEnum.OWNER.equals(member.getRole())) {
            Assert.isTrue(
                    enterpriseMemberService.countActiveOwners(organizationId) > 1,
                    "唯一企业主不能退出企业"
            );
        }

        member.setStatus(OrganizationMemberStatusEnum.DISABLED);
        enterpriseMemberService.save(member);
        switchOrganization(token, null);
    }

    private EnterpriseEntity requireEnabledOrganization(Long organizationId) {
        EnterpriseEntity organization = get(organizationId);
        Assert.notNull(organization, "找不到企业");
        Assert.isTrue(YesOrNo.Yes.equals(organization.getEnabled()), "企业已被禁用");
        return organization;
    }

    private void clearRemovedMemberContext(EnterpriseMemberEntity member) {
        TypeIdNameMetadata principal = IdNameMetadata.ofPrincipalString(member.getPrincipal());
        PersonalUserEntity user = personalUserService.getByIdentity(principal.getId());
        if (Objects.nonNull(user) && Objects.equals(user.getLastActiveOrganizationId(), member.getOrganizationId())) {
            personalUserService.lambdaUpdate()
                    .set(PersonalUserEntity::getLastActiveOrganizationId, null)
                    .eq(PersonalUserEntity::getId, user.getId())
                    .update();
        }
        accessTokenContextRepository.deleteSecurityContext(principal.getType(), principal.getId());
    }
}
