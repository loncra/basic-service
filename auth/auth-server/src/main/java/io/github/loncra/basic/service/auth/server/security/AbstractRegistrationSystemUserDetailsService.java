package io.github.loncra.basic.service.auth.server.security;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.auth.server.resolver.LoginTypeResolver;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.security.entity.support.SimpleSecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.support.MobileSecurityPrincipal;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author maurice.chen
 */
@Slf4j
@Getter
@Setter(onMethod_ = @Autowired)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractRegistrationSystemUserDetailsService<T extends AbstractBasicSystemUser> extends AbstractSystemUserDetailsService<T> {

    private CommonsConfig commonsConfig;

    private AccessTokenContextRepository accessTokenContextRepository;

    private List<LoginTypeResolver> loginTypeResolvers;

    @Override
    public SecurityPrincipal getSecurityPrincipal(TypeAuthenticationToken token) throws AuthenticationException {
        RequestAuthenticationToken requestAuthenticationToken = CastUtils.cast(token);
        String loginType = requestAuthenticationToken.getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);

        String username = loginTypeResolvers.stream()
                .filter(s -> s.isSupport(loginType))
                .findFirst()
                .orElseThrow(() -> new InternalAuthenticationServiceException(getType() + "用户不支持登录类型为 [" + loginType + "] 的登录模式"))
                .getUsername(requestAuthenticationToken);

        T user = getByIdentity(username);

        if (LoginTypeEnum.USERNAME_PASSWORD.toString().equals(loginType) && Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名密码错误");
        }

        if (Objects.nonNull(user) && UserStatus.Disabled.equals(user.getStatus())) {
            throw new DisabledException("您的账号已被禁用。");
        }

        return createSecurityPrincipal(user, requestAuthenticationToken);
    }

    protected SecurityPrincipal createSecurityPrincipal(
            T user,
            RequestAuthenticationToken token
    ) {

        SimpleSecurityPrincipal result;
        if (Objects.isNull(user)) {
            result = createTempSecurityPrincipal(token);
            result.setStatus(UserStatus.Enabled);
        }
        else {
            result = new SimpleSecurityPrincipal(user.getId(), user.getPassword(), user.getUsername(), user.getStatus());
        }

        return createSecurityPrincipal(token, result);
    }

    protected SimpleSecurityPrincipal createSecurityPrincipal(
            RequestAuthenticationToken token,
            SimpleSecurityPrincipal result
    ) {
        String deviceIdentified = token.getHeaderMap()
                .getFirst(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
        if (StringUtils.isEmpty(deviceIdentified)) {
            deviceIdentified = token.getParameterMap()
                    .getFirst(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
        }

        if (StringUtils.isNotEmpty(deviceIdentified)) {
            return new MobileSecurityPrincipal(result, deviceIdentified);
        }
        else {
            return result;
        }
    }

    protected @NonNull SimpleSecurityPrincipal createTempSecurityPrincipal(RequestAuthenticationToken token) {
        SimpleSecurityPrincipal result = new SimpleSecurityPrincipal();
        result.setUsername(commonsConfig.generateRandomUsername(token.getPrincipal().toString()));
        result.setCredentials(commonsConfig.generateRandomPassword());
        return result;
    }

    @Override
    public boolean matchesPassword(
            String presentedPassword,
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        RequestAuthenticationToken requestAuthenticationToken = CastUtils.cast(token);

        String loginType = requestAuthenticationToken.getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        return loginTypeResolvers.stream()
                .filter(s -> s.isSupport(loginType))
                .findFirst()
                .map(s -> s.matchesPassword(presentedPassword, requestAuthenticationToken, principal))
                .orElseGet(() -> super.matchesPassword(presentedPassword, token, principal));

    }

    @Override
    public Collection<GrantedAuthority> getPrincipalGrantedAuthorities(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        if (Objects.isNull(principal.getId())) {
            T user = createSystemUserEntity(token, principal);
            postCreateSystemUserEntity(user, token, principal);
            if (token instanceof RequestAuthenticationToken requestAuthenticationToken) {
                requestAuthenticationToken.getMetadata()
                        .put(PrincipalDetailsConstants.NEW_USER_KEY, true);
            }
        }
        return super.getPrincipalGrantedAuthorities(token, principal);
    }

    protected void postCreateSystemUserEntity(
            T user,
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {

    }

    private T createSystemUserEntity(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        T user = createOrmSystemUserEntity(token, principal);

        RequestAuthenticationToken requestAuthenticationToken = CastUtils.cast(token);
        String loginType = requestAuthenticationToken
                .getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);

        loginTypeResolvers.stream()
                .filter(s -> s.isSupport(loginType))
                .findFirst().ifPresent(s -> s.preInsertUser(requestAuthenticationToken, user));

        insertUser(user);
        principal.setId(user.getId());

        return user;
    }

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     */
    protected abstract void insertUser(T user);

    @Override
    public CacheProperties getAuthenticationCache(TypeAuthenticationToken token) {
        if (token instanceof RequestAuthenticationToken requestAuthenticationToken) {

            String loginType = requestAuthenticationToken
                    .getParameterMap()
                    .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);

            return LoginTypeEnum.LOAD_DATABASE_TYPES.contains(loginType) ? super.getAuthenticationCache(token) : null;
        }
        return super.getAuthenticationCache(token);
    }

    @Override
    public CacheProperties getAuthorizationCache(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        if (token instanceof RequestAuthenticationToken requestAuthenticationToken) {
            String loginType = requestAuthenticationToken.getParameterMap()
                    .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
            return LoginTypeEnum.LOAD_DATABASE_TYPES.contains(loginType) ? super.getAuthorizationCache(token, principal) : null;
        }
        else {
            return super.getAuthorizationCache(token, principal);
        }
    }

    protected T createOrmSystemUserEntity(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        throw new ServiceException(getType() + "不支持创建持久化系统用户实体操作");
    }

    @Override
    public boolean preSaveSecurityPrincipalCache(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        return Objects.nonNull(principal.getId());
    }
}
