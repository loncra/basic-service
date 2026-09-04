package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.api.constants.AuthenticationMqConstants;
import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseDao;
import io.github.loncra.basic.service.auth.server.domain.body.PersonalEnterpriseResponseBody;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberStatusEnum;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Getter
    private final EnterpriseMemberService enterpriseMemberService;

    private final EnterpriseInvitationService enterpriseInvitationService;

    private final PersonalUserService personalUserService;

    private final AccessTokenContextRepository accessTokenContextRepository;

    @Getter
    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(7);

    public EnterpriseEntity getAvailable(
            Long enterpriseId,
            String principal
    ) {
        if (Objects.isNull(enterpriseId)) {
            return null;
        }

        EnterpriseEntity organization = get(enterpriseId);
        if (Objects.isNull(organization) || !YesOrNo.Yes.equals(organization.getEnabled())) {
            return null;
        }

        EnterpriseMemberEntity member = enterpriseMemberService.getActiveMember(enterpriseId, principal);
        return Objects.nonNull(member) ? organization : null;
    }

    public void applyActiveMetadata(
            PersonalUserEntity user,
            AuditAuthenticationSuccessDetails details
    ) {
        EnterpriseEntity enterprise = getAvailable(
                user.getLastActiveOrganizationId(),
                user.getSystemName()
        );
        if (Objects.isNull(enterprise)) {
            details.getMetadata().put(TenantEntity.TENANT_ID_FIELD, user.getTenantId());
            details.getMetadata().remove(PrincipalDetailsConstants.ENTERPRISE_KEY);
        } else {
            details.getMetadata().put(TenantEntity.TENANT_ID_FIELD, enterprise.getTenantId());
            details.getMetadata().put(PrincipalDetailsConstants.ENTERPRISE_KEY, IdNameMetadata.of(enterprise.getId().toString(), enterprise.getName()));

            /*SpringSecurityTenantContext tenantContext = new SpringSecurityTenantContext(enterprise.getTenantId(), details.getMetadata());
            tenantContext.setType(token.getType());
            tenantContext.setPrincipal(token.getSecurityPrincipal());
            tenantContext.setLastAuthenticationTime(token.getLastAuthenticationTime());

            TenantContextHolder.set(tenantContext);*/
        }
    }

    public void switchByEnterpriseId(
            AuditAuthenticationToken token,
            Long enterpriseId
    ) {
        EnterpriseEntity enterprise = Objects.requireNonNull(get(enterpriseId), "找不到 ID 为 [" + enterpriseId + "] 企业");
        switchByEnterprise(token, enterprise);
    }

    public void switchByEnterprise(
            AuditAuthenticationToken token,
            EnterpriseEntity enterprise
    ) {

        PersonalUserEntity user = Objects.requireNonNull(
                personalUserService.getByIdentity(Objects.toString(token.getSecurityPrincipal().getId())),
                "找不到当前个人用户"
        );

        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        AuditAuthenticationToken newToken;
        if (Objects.nonNull(enterprise)) {
            EnterpriseMemberEntity memberEntity = Objects.requireNonNull(
                    enterpriseMemberService.getActiveMember(enterprise.getId(),token.getName()),
                    "当前用户不是该企业的有效成员，或企业已被禁用"
            );
            memberEntity.setLastAuthenticationTime(Instant.now());
            enterpriseMemberService.lambdaUpdate()
                    .set(EnterpriseMemberEntity::getLastAuthenticationTime, memberEntity.getLastAuthenticationTime())
                    .eq(IdEntity::getId, memberEntity.getId())
                    .update();
            personalUserService.lambdaUpdate()
                    .set(PersonalUserEntity::getLastActiveOrganizationId, enterprise.getId())
                    .eq(PersonalUserEntity::getId, user.getId())
                    .update();
            user.setLastActiveOrganizationId(enterprise.getId());

            applyActiveMetadata(user, details);
            newToken = createAuditAuthenticationToken(
                    token,
                    user.getLastAuthenticationTime(),
                    details,
                    authorities -> authorities.add(EnterpriseMemberRoleEnum.SECURITY_ROLE_PREFIX + memberEntity.getRole().toString())
            );
        } else {
            personalUserService.lambdaUpdate()
                    .set(PersonalUserEntity::getLastActiveOrganizationId, null)
                    .eq(PersonalUserEntity::getId, user.getId())
                    .update();
            user.setLastActiveOrganizationId(null);

            applyActiveMetadata(user, details);
            newToken = createAuditAuthenticationToken(
                    token,
                    user.getLastAuthenticationTime(),
                    details,
                    newPrincipalGrantedAuthorities -> {}
            );
        }

        accessTokenContextRepository.saveAuthentication(newToken);
    }

    private @NonNull AuditAuthenticationToken createAuditAuthenticationToken(
            AuditAuthenticationToken token,
            Instant lastAuthenticationTime,
            AuditAuthenticationSuccessDetails details,
            Consumer<Set<String>> grantedAuthorities
    ) {
        Set<String> principalGrantedAuthorities = token.getGrantedAuthorities()
                .stream()
                .filter(s -> !Strings.CS.startsWith(s, EnterpriseMemberRoleEnum.SECURITY_ROLE_PREFIX))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        grantedAuthorities.accept(principalGrantedAuthorities);

        Collection<? extends GrantedAuthority> authorities = principalGrantedAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        AuditAuthenticationToken auditAuthenticationToken = new AuditAuthenticationToken(
                token.getSecurityPrincipal(),
                token.getType(),
                authorities,
                lastAuthenticationTime
        );
        auditAuthenticationToken.setAuthenticated(true);
        auditAuthenticationToken.setRememberMe(token.isRememberMe());
        auditAuthenticationToken.setDetails(details);
        return auditAuthenticationToken;
    }

    @Transactional(rollbackFor = Exception.class)
    public EnterpriseEntity save(
            AuditAuthenticationToken token,
            EnterpriseEntity body
    ) {
        EnterpriseEntity organization;
        if (Objects.nonNull(body.getId())) {
            organization = body;
            updateById(body);
        } else {
            organization = CastUtils.of(body, EnterpriseEntity.class);
            organization.setOwnerPrincipal(token.getName());
            organization.setEnabled(YesOrNo.Yes);
            organization.setTenantId(snowflakeIdGenerator.generateId());
            insert(organization);

            EnterpriseMemberEntity owner = new EnterpriseMemberEntity();
            owner.setEnterpriseId(organization.getId());
            owner.setPrincipal(token.getName());
            owner.setRole(EnterpriseMemberRoleEnum.OWNER);
            owner.setStatus(EnterpriseMemberStatusEnum.ACTIVE);
            enterpriseMemberService.insert(owner);

            switchByEnterprise(token, organization);
        }
        return organization;
    }

    public List<PersonalEnterpriseResponseBody> findByPrincipal(String principal) {
        List<Long> organizationIds = enterpriseMemberService.findActiveByPrincipal(principal)
                .stream()
                .map(EnterpriseMemberEntity::getEnterpriseId)
                .distinct()
                .toList();
        if (organizationIds.isEmpty()) {
            return List.of();
        }
        List<EnterpriseEntity> result = lambdaQuery()
                .in(EnterpriseEntity::getId, organizationIds)
                .list();

        return result.stream().map(r -> convertPersonalEnterpriseResponseBody(r, principal)).toList();
    }

    private PersonalEnterpriseResponseBody convertPersonalEnterpriseResponseBody(
            EnterpriseEntity enterpriseEntity,
            String principal
    ) {

        PersonalEnterpriseResponseBody body = CastUtils.of(enterpriseEntity, PersonalEnterpriseResponseBody.class);
        EnterpriseMemberEntity member = enterpriseMemberService.getMember(enterpriseEntity.getId(),principal);
        if (Objects.nonNull(member)) {
            body.setRole(member.getRole());
            body.setStatus(member.getStatus());
        }
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public EnterpriseInvitationEntity invite(
            AuditAuthenticationToken token,
            Long organizationId,
            String phoneNumber
    ) {
        requireEnabledEnterprise(organizationId);
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
                    Objects.isNull(member) || EnterpriseMemberStatusEnum.DISABLED.equals(member.getStatus()),
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
            pending.setStatus(EnterpriseInvitationStatusEnum.EXPIRED);
            enterpriseInvitationService.save(pending);
        }

        EnterpriseInvitationEntity invitation = new EnterpriseInvitationEntity();
        invitation.setEnterpriseId(organizationId);
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
                EnterpriseInvitationStatusEnum.PENDING.equals(invitation.getStatus()),
                "企业邀请已失效"
        );
        Assert.isTrue(invitation.getExpirationTime().isAfter(Instant.now()), "企业邀请已过期");
        requireEnabledEnterprise(invitation.getEnterpriseId());

        PersonalUserEntity user = personalUserService.getByIdentity(Objects.toString(token.getSecurityPrincipal().getId()));
        Assert.notNull(user, "找不到当前个人用户");
        Assert.isTrue(
                Objects.equals(user.getPhoneNumber(), invitation.getPhoneNumber()),
                "当前用户手机号与企业邀请不一致"
        );

        EnterpriseMemberEntity member = enterpriseMemberService.getMember(
                invitation.getEnterpriseId(),
                token.getName()
        );
        if (Objects.isNull(member)) {
            member = new EnterpriseMemberEntity();
            member.setEnterpriseId(invitation.getEnterpriseId());
            member.setPrincipal(token.getName());
            member.setRole(EnterpriseMemberRoleEnum.MEMBER);
        }
        member.setStatus(EnterpriseMemberStatusEnum.ACTIVE);
        enterpriseMemberService.save(member);

        invitation.setStatus(EnterpriseInvitationStatusEnum.ACCEPTED);
        enterpriseInvitationService.save(invitation);
    }

    public List<EnterpriseMemberEntity> findMembers(
            AuditAuthenticationToken token,
            Long enterpriseId
    ) {
        Objects.requireNonNull(
                enterpriseMemberService.getActiveMember(enterpriseId,token.getName()),
                "当前用户不是该企业的有效成员，或企业已被禁用"
        );
        return enterpriseMemberService.findByEnterpriseId(enterpriseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeMember(
            AuditAuthenticationToken token,
            Long organizationId,
            String principal
    ) {
        requireEnabledEnterprise(organizationId);
        enterpriseMemberService.requireManager(organizationId, token.getName());
        Assert.isTrue(!Objects.equals(token.getName(), principal), "不能通过移除成员退出企业");

        EnterpriseMemberEntity member = Objects.requireNonNull(
                enterpriseMemberService.getActiveMember(organizationId, principal),
                "找不到有效的企业成员"
        );
        SystemException.isTrue(
                !EnterpriseMemberRoleEnum.OWNER.equals(member.getRole()),
                "不能移除企业主"
        );
        member.setStatus(EnterpriseMemberStatusEnum.DISABLED);
        enterpriseMemberService.save(member);
        clearRemovedMemberContext(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public void leave(
            AuditAuthenticationToken token,
            Long enterpriseId
    ) {
        EnterpriseMemberEntity member = Objects.requireNonNull(
                enterpriseMemberService.getActiveMember(enterpriseId,token.getName()),
                "当前用户不是该企业的有效成员"
        );
        if (EnterpriseMemberRoleEnum.OWNER.equals(member.getRole())) {
            boolean execute = lambdaUpdate().set(EnterpriseEntity::getDisbandTime, Instant.now())
                    .set(EnterpriseEntity::getEnabled, YesOrNo.No.getValue())
                    .eq(IdEntity::getId, enterpriseId)
                    .update();
            SystemException.isTrue(execute, "解散 ID 为 [" + enterpriseId + "] 企业失败");
            List<EnterpriseMemberEntity> members = enterpriseMemberService.findByEnterpriseId(enterpriseId);
            members.stream()
                    .peek(m -> m.setStatus(EnterpriseMemberStatusEnum.DISABLED))
                    .forEach(enterpriseMemberService::updateById);

            List<EnterpriseInvitationEntity> invitations = enterpriseInvitationService.findPendingByEnterpriseId(enterpriseId);
            invitations.stream()
                    .peek(m -> m.setStatus(EnterpriseInvitationStatusEnum.CANCELLED))
                    .forEach(enterpriseInvitationService::updateById);
        } else {
            member.setStatus(EnterpriseMemberStatusEnum.DISABLED);
            enterpriseMemberService.save(member);
        }
        switchByEnterprise(token, null);
    }

    private EnterpriseEntity requireEnabledEnterprise(Long enterpriseId) {
        EnterpriseEntity organization = Objects.requireNonNull(get(enterpriseId), "找不到 ID 为 [" + enterpriseId + "] 企业");
        SystemException.isTrue(YesOrNo.Yes.equals(organization.getEnabled()), "企业已被禁用");
        return organization;
    }

    private void clearRemovedMemberContext(EnterpriseMemberEntity member) {
        TypeIdNameMetadata principal = IdNameMetadata.ofPrincipalString(member.getPrincipal());
        PersonalUserEntity user = personalUserService.getByIdentity(principal.getId());
        if (Objects.nonNull(user) && Objects.equals(user.getLastActiveOrganizationId(), member.getEnterpriseId())) {
            personalUserService.lambdaUpdate()
                    .set(PersonalUserEntity::getLastActiveOrganizationId, null)
                    .eq(PersonalUserEntity::getId, user.getId())
                    .update();
        }
        accessTokenContextRepository.deleteSecurityContext(principal.getType(), principal.getId());
    }

    public void export(ExportDataMetadata dto) {
        String cacheName = SystemConstants.USER_EXPORT_CACHE.getName(dto.toExportCacheName());
        RBucket<ExportDataMetadata> bucket = redissonClient.getBucket(cacheName);
        if (bucket.isExists()) {
            return ;
        }
        bucket.set(dto, SystemConstants.USER_EXPORT_CACHE.getExpiresTime().toDuration());
        amqpTemplate.convertAndSend(SystemConstants.SYS_AUTH_RABBITMQ_EXCHANGE, AuthenticationMqConstants.ENTERPRISE_EXPORT_QUEUE_NAME, dto.toExportCacheName());
    }
}
