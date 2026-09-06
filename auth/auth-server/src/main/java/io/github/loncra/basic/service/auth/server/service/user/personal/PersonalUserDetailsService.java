package io.github.loncra.basic.service.auth.server.service.user.personal;

import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.security.AbstractRegistrationSystemUserDetailsService;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.tenant.holder.TenantContextHolder;
import io.github.loncra.framework.security.entity.RoleAuthority;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.security.entity.support.SimpleSecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.SpringSecurityTenantContext;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * 个人用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class PersonalUserDetailsService extends AbstractRegistrationSystemUserDetailsService<PersonalUserEntity> {

    private final PersonalUserService personalUserService;

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    private final EnterpriseService enterpriseService;

    @Override
    protected PersonalUserEntity getByIdentity(String id) {
        return personalUserService.getByIdentity(id);
    }

    @Override
    protected void updateLastAuthenticationTime(
            Object id,
            Instant date
    ) {
        personalUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getLastAuthenticationTime, date)
                .eq(AbstractPlatformUser::getId, id)
                .update();
    }

    @Override
    public List<String> getType() {
        return List.of(ResourceSourceEnum.PERSONAL_SOURCE_VALUE, ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return personalUserService.getPasswordEncoder();
    }

    @Override
    protected void insertUser(PersonalUserEntity user) {
        personalUserService.insert(user);
    }

    @Override
    protected @NonNull SimpleSecurityPrincipal createTempSecurityPrincipal(RequestAuthenticationToken token) {
        RequestAuthenticationToken requestAuthenticationToken = CastUtils.cast(token);
        String loginType = requestAuthenticationToken.getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        if (LoginTypeEnum.USERNAME_PASSWORD_REGISTER.toString().equals(loginType)) {
            SimpleSecurityPrincipal result = new SimpleSecurityPrincipal();
            result.setUsername(token.getPrincipal().toString());
            result.setCredentials(getPasswordEncoder().encode(token.getCredentials().toString()));
            return result;
        } else {
            return super.createTempSecurityPrincipal(token);
        }
    }

    @Override
    public Collection<GrantedAuthority> getPrincipalGrantedAuthorities(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        if (token instanceof RequestAuthenticationToken requestAuthenticationToken) {

            Object enterprise = requestAuthenticationToken.getMetadata().get(PrincipalDetailsConstants.ENTERPRISE_KEY);
            if (Objects.isNull(enterprise)) {
                return super.getPrincipalGrantedAuthorities(token, principal);
            }

            EnterpriseMemberEntity member = enterpriseService.getEnterpriseMemberService()
                    .get(principal.getId().toString());
            List<RoleEntity> roleAuthorityMetadataList = member.getRoleIds()
                    .stream()
                    .map(id -> getRoleService().get(id))
                    .filter(r -> YesOrNo.Yes.equals(r.getEnabled()))
                    .toList();
            List<ResourceEntity> resourceMetadataList = getPluginResourceService().getResourcesStream(
                    member.getResourceIds(),
                    ResourceSourceEnum.ENTERPRISE
            );

            Collection<GrantedAuthority> result = new HashSet<>(createGrantedAuthorities(roleAuthorityMetadataList, resourceMetadataList));
            result.add(new SimpleGrantedAuthority(EnterpriseMemberRoleEnum.SECURITY_ROLE_PREFIX + member.getRole().toString()));

            return result;
        } else {
            return super.getPrincipalGrantedAuthorities(token, principal);
        }
    }

    @Override
    public AuditAuthenticationToken createSuccessAuthentication(
            SecurityPrincipal principal,
            TypeAuthenticationToken token,
            Collection<? extends GrantedAuthority> grantedAuthorities
    ) {
        if (token instanceof RequestAuthenticationToken requestAuthenticationToken) {
            Object enterprise = requestAuthenticationToken.getMetadata().get(PrincipalDetailsConstants.ENTERPRISE_KEY);
            if (Objects.isNull(enterprise)) {
                return super.createSuccessAuthentication(principal, token, grantedAuthorities);
            }
            AuditAuthenticationToken result = new AuditAuthenticationToken(
                    principal,
                    ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE,
                    grantedAuthorities,
                    Instant.now()
            );
            result.setAuthenticated(true);
            result.setRememberMe(false);

            enterpriseService.getEnterpriseMemberService()
                    .lambdaUpdate()
                    .set(EnterpriseMemberEntity::getLastAuthenticationTime, result.getLastAuthenticationTime())
                    .eq(IdEntity::getId, principal.getId())
                    .update();

            AuditAuthenticationSuccessDetails successDetails = getPrincipalDetails(principal, token, result, grantedAuthorities);
            successDetails.setRemember(result.isRememberMe());
            successDetails.getMetadata().put(PrincipalDetailsConstants.ENTERPRISE_KEY, enterprise);

            result.setDetails(successDetails);
            return result;
        }

        return super.createSuccessAuthentication(principal, token, grantedAuthorities);
    }

    @Override
    protected void preGetPrincipalDetails(
            AuditAuthenticationSuccessDetails details,
            SecurityPrincipal principal,
            AuditAuthenticationToken successToken
    ) {
        if (ResourceSourceEnum.PERSONAL_SOURCE_VALUE.equals(successToken.getType())) {
            super.preGetPrincipalDetails(details, principal, successToken);
        } else {
            EnterpriseMemberEntity member = enterpriseService.getEnterpriseMemberService()
                    .get(principal.getId().toString());
            if (CollectionUtils.isNotEmpty(member.getRoleIds())) {
                List<RoleAuthority> roles = member.getRoleIds()
                        .stream()
                        .map(id -> getRoleService().get(id))
                        .map(s -> CastUtils.of(s, RoleAuthority.class))
                        .toList();
                details.getMetadata()
                        .put(SystemConstants.ROLE_FIELD_NAME, roles);
            }
            enterpriseService.getEnterpriseMemberService()
                    .setPersonalUser(member);
            details.getMetadata()
                    .putAll(member.toPrincipalMetadata());
        }
    }

    @Override
    public CacheProperties getAuthenticationCache(TypeAuthenticationToken token) {
        return null;
    }

    @Override
    public CacheProperties getAuthorizationCache(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        return null;
    }

    @Override
    protected SecurityPrincipal createSecurityPrincipal(
            PersonalUserEntity user,
            RequestAuthenticationToken token
    ) {
        if (Objects.isNull(user)) {
            return super.createSecurityPrincipal(null, token);
        }
        EnterpriseEntity enterprise = null;
        if (Objects.nonNull(user.getLastActiveEnterpriseId())) {
            enterprise = enterpriseService.get(user.getLastActiveEnterpriseId());
        }
        EnterpriseMemberEntity member = null;
        if (Objects.nonNull(enterprise)) {
            TenantContextHolder.set(new SpringSecurityTenantContext(enterprise.getTenantId(), token.getMetadata()));
            member = enterpriseService.getEnterpriseMemberService()
                    .getActiveMember(enterprise.getId(), user.getSystemName());
        }

        if (Objects.nonNull(member)) {
            IdNameMetadata metadata = IdNameMetadata.of(enterprise.getId().toString(), enterprise.getName());
            token.getMetadata()
                    .put(PrincipalDetailsConstants.ENTERPRISE_KEY, metadata);

            SimpleSecurityPrincipal securityPrincipal = new SimpleSecurityPrincipal(
                    member.getId(),
                    user.getPassword(),
                    member.getUsername(),
                    member.getStatus()
            );
            return createSecurityPrincipal(token, securityPrincipal);
        }
        return super.createSecurityPrincipal(user, token);
    }

    @Override
    protected void postGetPrincipalGrantedAuthorities(
            PersonalUserEntity user,
            TypeAuthenticationToken token,
            SecurityPrincipal principal,
            Collection<GrantedAuthority> result
    ) {
        if (Objects.isNull(user.getLastActiveEnterpriseId())) {
            return ;
        }

        EnterpriseMemberEntity member = enterpriseService.getEnterpriseMemberService()
                .getActiveMember(user.getLastActiveEnterpriseId(), user.getSystemName());
        String role = EnterpriseMemberRoleEnum.SECURITY_ROLE_PREFIX + member.getRole().toString();
        result.add(new SimpleGrantedAuthority(role));
    }

    @Override
    protected PersonalUserEntity createOrmSystemUserEntity(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        RequestAuthenticationToken requestAuthenticationToken = CastUtils.cast(token);
        String loginType = requestAuthenticationToken.getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);

        PersonalUserEntity user = new PersonalUserEntity();

        if (LoginTypeEnum.USERNAME_PASSWORD_REGISTER.toString().equals(loginType)) {
            user.setUsername(principal.getUsername());
            user.setPassword(principal.getCredentials().toString());

            user.getInitialization().setRandomPassword(YesOrNo.No);
            user.getInitialization().setRandomUsername(YesOrNo.No);
        } else if (LoginTypeEnum.PHONE_CAPTCHA.toString().equals(loginType)){
            user.setUsername(getCommonsConfig().generateRandomUsername(principal.getUsername()));
            user.setPassword(getPasswordEncoder().encode(getCommonsConfig().generateRandomPassword()));
        } else {
            throw new SystemException("支持非 [" + LoginTypeEnum.LOAD_DATABASE_TYPES + "] 类型的登录方式进行创建用户");
        }

        user.setNickname(getCommonsConfig().generateRandomNickName());
        user.setStatus(UserStatus.Enabled);
        user.setTenantId(snowflakeIdGenerator.generateId());

        RoleEntity role = personalUserService.getRoleService().getByAuthority(ResourceSourceEnum.PERSONAL.getAdminAuthority().getId());
        if (Objects.nonNull(role)) {
            user.setRoleIds(Set.of(role.getId()));
        }

        return user;
    }
}
