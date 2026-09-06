package io.github.loncra.basic.service.auth.server.security;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.PluginResourceService;
import io.github.loncra.basic.service.auth.server.service.role.RoleService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.domain.AccessToken;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
import io.github.loncra.framework.security.entity.ResourceAuthority;
import io.github.loncra.framework.security.entity.RoleAuthority;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.security.entity.support.SimpleSecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.AbstractTypeSecurityPrincipalService;
import io.github.loncra.framework.spring.security.core.authentication.config.AuthenticationProperties;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.security.core.entity.support.AccessTokenAuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.security.core.plugin.PluginEndpoint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;

import java.time.Instant;
import java.util.*;

/**
 * 抽象的系统用户认证类
 *
 * @author maurice.chen
 */
@Getter
@Setter(onMethod_ = @Autowired)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractSystemUserDetailsService<T extends AbstractBasicSystemUser> extends AbstractTypeSecurityPrincipalService {

    private PluginResourceService pluginResourceService;

    private RoleService roleService;

    private OpenPlatformMerchantService openPlatformMerchantService;

    private JwtGenerator jwtGenerator;

    @Override
    @Autowired
    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        super.setAuthenticationProperties(authenticationProperties);
    }

    public static Collection<SimpleGrantedAuthority> createGrantedAuthorities(
            List<RoleEntity> roleAuthorities,
            List<ResourceEntity> resourceAuthorities
    ) {

        List<SimpleGrantedAuthority> result = new ArrayList<>();

        resourceAuthorities.stream()
                .filter(x -> StringUtils.isNotBlank(x.getAuthority()))
                .filter(x -> !PluginEndpoint.DEFAULT_IS_AUTHENTICATED_METHOD_NAME.equals(x.getAuthority()))
                .flatMap(x -> Arrays.stream(StringUtils.split(x.getAuthority(), CastUtils.COMMA)))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotEmpty)
                .filter(x -> Strings.CS.startsWith(x, ResourceAuthority.DEFAULT_RESOURCE_PREFIX))
                .filter(x -> Strings.CS.endsWith(x, ResourceAuthority.DEFAULT_RESOURCE_SUFFIX))
                .map(SimpleGrantedAuthority::new)
                .distinct()
                .forEach(result::add);

        roleAuthorities.stream()
                .map(x -> RoleAuthority.DEFAULT_ROLE_PREFIX + x.getAuthority())
                .map(SimpleGrantedAuthority::new)
                .distinct()
                .forEach(result::add);

        return result;
    }

    @Override
    public Collection<GrantedAuthority> getPrincipalGrantedAuthorities(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {

        T user = getByIdentity(principal.getId().toString());

        List<RoleEntity> roleAuthorityMetadataList = user
                .getRoleIds()
                .stream()
                .map(roleService::get)
                .filter(r -> YesOrNo.Yes.equals(r.getEnabled()))
                .toList();
        List<ResourceEntity> resourceMetadataList = pluginResourceService.getResourcesStream(user.getResourceIds(), ResourceSourceEnum.valueOf(token.getType()));

        Collection<GrantedAuthority> result = new HashSet<>(createGrantedAuthorities(roleAuthorityMetadataList, resourceMetadataList));
        postGetPrincipalGrantedAuthorities(user, token, principal, result);
        return result;
    }

    protected void postGetPrincipalGrantedAuthorities(
            T user,
            TypeAuthenticationToken token,
            SecurityPrincipal principal,
            Collection<GrantedAuthority> result
    ) {

    }

    /**
     * 通过主键 id 获取用户信息
     *
     * @param id 主键 id
     * @return 用户信息
     */
    protected abstract T getByIdentity(String id);

    protected void preGetPrincipalDetails(
            AuditAuthenticationSuccessDetails details,
            SecurityPrincipal principal,
            AuditAuthenticationToken successToken
    ) {
        T user = getByIdentity(principal.getId().toString());
        if (CollectionUtils.isNotEmpty(user.getRoleIds())) {
            List<RoleAuthority> roles = user.getRoleIds()
                    .stream()
                    .map(roleService::get)
                    .map(s -> CastUtils.of(s, RoleAuthority.class))
                    .toList();
            details.getMetadata()
                    .put(SystemConstants.ROLE_FIELD_NAME, roles);
        }
        details.getMetadata()
                .putAll(user.toPrincipalMetadata());
    }

    @Override
    public AuditAuthenticationSuccessDetails getPrincipalDetails(
            SecurityPrincipal principal,
            TypeAuthenticationToken token,
            AuditAuthenticationToken successToken,
            Collection<? extends GrantedAuthority> grantedAuthorities
    ) {

        AuditAuthenticationSuccessDetails details = super.getPrincipalDetails(principal, token, successToken, grantedAuthorities);

        preGetPrincipalDetails(details, principal, successToken);

        AccessToken accessToken = openPlatformMerchantService.createInternalAccessToken(successToken);
        if (Objects.isNull(accessToken)) {
            return details;
        }

        return new AccessTokenAuditAuthenticationSuccessDetails(details, accessToken);
    }

    @Override
    public AuditAuthenticationToken createSuccessAuthentication(
            SecurityPrincipal principal,
            TypeAuthenticationToken token,
            Collection<? extends GrantedAuthority> grantedAuthorities
    ) {
        updateLastAuthenticationTime(principal.getId(), Instant.now());

        return super.createSuccessAuthentication(principal, token, grantedAuthorities);
    }

    @Override
    public CacheProperties getAuthorizationCache(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        if (Objects.isNull(principal)) {

            return CacheProperties.of(
                    getAuthenticationProperties().getAuthorizationCache()
                            .getName(token.getType() + CacheProperties.DEFAULT_SEPARATOR + DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL),
                    getAuthenticationProperties().getAuthorizationCache()
                            .getExpiresTime()
            );
        }
        return super.getAuthorizationCache(token, principal);
    }

    /**
     * 更新最后登录时间
     *
     * @param id   主键 id
     * @param date 最后登录时间
     */
    protected abstract void updateLastAuthenticationTime(
            Object id,
            Instant date
    );

    @Override
    public SecurityPrincipal getSecurityPrincipal(TypeAuthenticationToken token) throws AuthenticationException {
        T user = getByIdentity(token.getPrincipal()
                .toString());

        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        if (UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("您的账号已被禁用。");
        }

        return new SimpleSecurityPrincipal(user.getId(), user.getPassword(), user.getUsername(), user.getStatus());
    }
}
