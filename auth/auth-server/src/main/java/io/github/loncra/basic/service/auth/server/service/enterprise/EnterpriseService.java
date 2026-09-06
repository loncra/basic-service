package io.github.loncra.basic.service.auth.server.service.enterprise;

import io.github.loncra.basic.service.auth.api.constants.AuthenticationMqConstants;
import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.dao.enterprise.EnterpriseDao;
import io.github.loncra.basic.service.auth.server.domain.body.PersonalEnterpriseResponseBody;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.domain.ExpiredToken;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.tenant.TenantContext;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.security.entity.RoleAuthority;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.security.entity.support.SimpleSecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.SpringSecurityTenantContext;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.security.core.entity.support.AccessTokenAuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.security.core.entity.support.MobileSecurityPrincipal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
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

    private final AccessTokenContextRepository accessTokenContextRepository;

    private final OpenPlatformMerchantService openPlatformMerchantService;

    @Getter
    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Transactional(rollbackFor = Exception.class)
    public String switchByEnterpriseId(
            AuditAuthenticationToken token,
            Long enterpriseId
    ) {
        EnterpriseEntity enterprise = Objects.requireNonNull(get(enterpriseId), "找不到 ID 为 [" + enterpriseId + "] 企业");
        return switchByEnterprise(token, enterprise);
    }

    @Transactional(rollbackFor = Exception.class)
    public String switchByEnterprise(
            AuditAuthenticationToken token,
            EnterpriseEntity enterprise
    ) {

        PersonalUserEntity user;
        if (ResourceSourceEnum.PERSONAL_SOURCE_VALUE.equals(token.getType())) {
            String id = token.getSecurityPrincipal().getId().toString();
            user = Objects.requireNonNull(
                    enterpriseMemberService.getPersonalUserService().getByIdentity(id),
                    "找不到 ID 为 [" + id + "] 个人用户"
            );
        } else if (ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE.equals(token.getType())) {
            AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
            String principal = Objects.toString(details.getMetadata().get(PrincipalDetailsConstants.PRINCIPAL_KEY), StringUtils.EMPTY);
            TypeIdNameMetadata metadata = TypeIdNameMetadata.ofPrincipalString(principal);
            user = Objects.requireNonNull(
                    enterpriseMemberService.getPersonalUserService().getByIdentity(metadata.getId()),
                    "找不到 ID 为 [" + metadata.getId() + "] 个人用户"
            );
        } else {
            throw new SystemException("不支持的认证类型为 [" + token.getType() + "] 切换企业");
        }

        AuditAuthenticationToken newToken;
        if (Objects.nonNull(enterprise)) {

            EnterpriseMemberEntity enterpriseMember = Objects.requireNonNull(
                    enterpriseMemberService.getActiveMember(enterprise.getId(),user.getSystemName()),
                    "当前用户不是该企业的有效成员，或企业已被禁用"
            );
            enterpriseMemberService.setPersonalUser(enterpriseMember);
            user.setLastActiveEnterpriseId(enterprise.getId());
            enterpriseMember.setLastAuthenticationTime(Instant.now());
            enterpriseMemberService.lambdaUpdate()
                    .set(EnterpriseMemberEntity::getLastAuthenticationTime, enterpriseMember.getLastAuthenticationTime())
                    .eq(IdEntity::getId, enterpriseMember.getId())
                    .update();

            SecurityPrincipal principal = createSecurityPrincipal(enterpriseMember, enterprise.getTenantId(), token);
            Collection<SimpleGrantedAuthority> grantedAuthorities = enterpriseMemberService.getAuthorities(enterpriseMember);
            newToken = copyAuditAuthenticationTokenDetail(principal, token, enterpriseMember, grantedAuthorities);
        } else {
            user.setLastActiveEnterpriseId(null);
            user.setLastAuthenticationTime(Instant.now());
            enterpriseMemberService.getPersonalUserService()
                    .lambdaUpdate()
                    .set(PersonalUserEntity::getLastActiveEnterpriseId, null)
                    .set(PersonalUserEntity::getLastAuthenticationTime, user.getLastAuthenticationTime())
                    .eq(IdEntity::getId, user.getId())
                    .update();
            SecurityPrincipal principal = createSecurityPrincipal(user, user.getTenantId(), token);
            Collection<SimpleGrantedAuthority> grantedAuthorities = enterpriseMemberService.getPersonalUserService()
                    .getAuthorities(user);
            newToken = copyAuditAuthenticationTokenDetail(principal, token, user,grantedAuthorities);
        }
        accessTokenContextRepository.deleteSecurityContext(token.getType(), token.getSecurityPrincipal().getId());
        accessTokenContextRepository.saveAuthentication(newToken);

        if (newToken.getDetails() instanceof AccessTokenAuditAuthenticationSuccessDetails details) {
            return details.getToken().getValue();
        }
        return null;
    }

    private AuditAuthenticationToken copyAuditAuthenticationTokenDetail(
            SecurityPrincipal principal,
            AuditAuthenticationToken token,
            AbstractBasicSystemUser user,
            Collection<SimpleGrantedAuthority> grantedAuthorities
    ) {

        AuditAuthenticationToken result = new AuditAuthenticationToken(
                principal,
                user.getType().toString(),
                grantedAuthorities,
                user.getLastAuthenticationTime()
        );
        result.setAuthenticated(token.isAuthenticated());
        result.setRememberMe(token.isRememberMe());

        Map<String, Object> metadata = user.toPrincipalMetadata();
        List<RoleAuthority> roles = user.getRoleIds()
                .stream()
                .map(id -> enterpriseMemberService.getPersonalUserService().getRoleService().get(id))
                .map(s -> CastUtils.of(s, RoleAuthority.class))
                .collect(Collectors.toCollection(LinkedList::new));

        Object currentDetails = token.getDetails();
        if (currentDetails instanceof AccessTokenAuditAuthenticationSuccessDetails details) {
            ExpiredToken expiredToken;
            if (user instanceof PersonalUserEntity) {
                expiredToken = openPlatformMerchantService.createInternalAccessToken(result);
            } else if (user instanceof EnterpriseMemberEntity member){
                expiredToken = openPlatformMerchantService.createAccessToken(member.getTenantId(), result);
                RoleAuthority roleAuthority = new RoleAuthority(member.getRole().getName(), EnterpriseMemberRoleEnum.SECURITY_ROLE_PREFIX + member.getRole());
                roles.add(roleAuthority);
            } else {
                throw new SystemException("不支持的用户类型为 [" + user.getType().getName() + "] 拷贝认证 token");
            }

            metadata.put(SystemConstants.ROLE_FIELD_NAME, roles);
            AccessTokenAuditAuthenticationSuccessDetails newDetails = new AccessTokenAuditAuthenticationSuccessDetails(
                    details.getRequestDetails(),
                    metadata,
                    expiredToken
            );
            result.setDetails(newDetails);
        } else if (currentDetails instanceof AuditAuthenticationSuccessDetails details) {
            AuditAuthenticationSuccessDetails newDetails = new AuditAuthenticationSuccessDetails(
                    details.getRequestDetails(),
                    user.toPrincipalMetadata()
            );
            result.setDetails(newDetails);
        } else {
            result.setDetails(token.getDetails());
        }
        return result;
    }

    private SecurityPrincipal createSecurityPrincipal(
            AbstractBasicSystemUser user,
            String tenantId,
            AuditAuthenticationToken token
    ) {
        TenantContext tenantContext = createSpringSecurityTenantContext(tenantId, token);
        TenantContextHolder.set(tenantContext);
        SecurityPrincipal principal = new SimpleSecurityPrincipal(
                user.getId(),
                user.getPassword(),
                user.getUsername(),
                user.getStatus()
        );
        if (token.getPrincipal() instanceof MobileSecurityPrincipal mobile) {
            principal = new MobileSecurityPrincipal(principal, mobile.getDeviceIdentified());
        }
        return principal;
    }

    private SpringSecurityTenantContext createSpringSecurityTenantContext(
            String tenantId,
            AuditAuthenticationToken token
    ) {
        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        SpringSecurityTenantContext result = new SpringSecurityTenantContext(tenantId, details.getMetadata());
        result.setPrincipal(token.getSecurityPrincipal());
        result.setType(token.getType());
        result.setLastAuthenticationTime(token.getLastAuthenticationTime());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public IdValueMetadata<String, EnterpriseEntity> save(
            AuditAuthenticationToken token,
            EnterpriseEntity body
    ) {

        EnterpriseEntity value;
        AuditAuthenticationToken currentToken;
        if (Objects.nonNull(body.getId())) {
            value = body;
            updateById(body);
            currentToken = token;
        } else {
            value = CastUtils.of(body, EnterpriseEntity.class);
            value.setOwnerPrincipal(token.getName());
            value.setEnabled(YesOrNo.Yes);
            value.setTenantId(snowflakeIdGenerator.generateId());
            insert(value);

            OpenPlatformMerchantEntity merchant = new OpenPlatformMerchantEntity();
            merchant.setName(value.getName());
            merchant.setAppId(value.getTenantId());
            openPlatformMerchantService.save(merchant);

            EnterpriseMemberEntity owner = enterpriseMemberService.createOwner(value, token.getName());

            SecurityPrincipal principal = createSecurityPrincipal(owner, value.getTenantId(), token);
            Collection<SimpleGrantedAuthority> grantedAuthorities = enterpriseMemberService.getAuthorities(owner);
            currentToken = copyAuditAuthenticationTokenDetail(principal, token, owner, grantedAuthorities);

            accessTokenContextRepository.deleteSecurityContext(token.getType(), token.getSecurityPrincipal().getId());
            accessTokenContextRepository.saveAuthentication(currentToken);
        }

        if (currentToken.getDetails() instanceof AccessTokenAuditAuthenticationSuccessDetails details) {
            return IdValueMetadata.of(details.getToken().getValue(), value);
        }

        return IdValueMetadata.of(value.getId().toString(), value);
    }

    public List<PersonalEnterpriseResponseBody> findByPrincipal(String principal) {
        List<Long> enterpriseIds = enterpriseMemberService.findActiveByPrincipal(principal)
                .stream()
                .map(EnterpriseMemberEntity::getEnterpriseId)
                .distinct()
                .toList();
        if (enterpriseIds.isEmpty()) {
            return List.of();
        }
        List<EnterpriseEntity> result = lambdaQuery()
                .in(EnterpriseEntity::getId, enterpriseIds)
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
            body.setStatus(member.getInvitation());
        }
        return body;
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
                    .peek(m -> m.setStatus(UserStatus.Disabled))
                    .forEach(this::clearRemovedMemberContext);

            List<EnterpriseInvitationEntity> invitations = enterpriseInvitationService.findPendingByEnterpriseId(enterpriseId);
            invitations.stream()
                    .peek(m -> m.setStatus(EnterpriseInvitationStatusEnum.CANCELLED))
                    .forEach(enterpriseInvitationService::updateById);
        } else {
            clearRemovedMemberContext(member);
        }
        switchByEnterprise(token, null);
    }

    private void clearRemovedMemberContext(EnterpriseMemberEntity member) {
        TypeIdNameMetadata principal = IdNameMetadata.ofPrincipalString(member.getPrincipal());
        PersonalUserEntity user = enterpriseMemberService.getPersonalUserService()
                .getByIdentity(principal.getId());
        if (Objects.nonNull(user) && Objects.equals(user.getLastActiveEnterpriseId(), member.getEnterpriseId())) {
            enterpriseMemberService.getPersonalUserService()
                    .lambdaUpdate()
                    .set(AbstractBasicSystemUser::getStatus, member.getStatus())
                    .set(PersonalUserEntity::getLastActiveEnterpriseId, null)
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
