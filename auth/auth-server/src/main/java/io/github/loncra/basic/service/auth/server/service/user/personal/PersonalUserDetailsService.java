package io.github.loncra.basic.service.auth.server.service.user.personal;

import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.RoleEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.auth.server.security.AbstractRegistrationSystemUserDetailsService;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.security.entity.support.SimpleSecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        return List.of(ResourceSourceEnum.PERSONAL_SOURCE_VALUE);
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
    public AuditAuthenticationSuccessDetails getPrincipalDetails(
            SecurityPrincipal principal,
            TypeAuthenticationToken token,
            AuditAuthenticationToken successToken,
            Collection<? extends GrantedAuthority> grantedAuthorities
    ) {
        AuditAuthenticationSuccessDetails details = super.getPrincipalDetails(
                principal,
                token,
                successToken,
                grantedAuthorities
        );
        PersonalUserEntity user = personalUserService.getByIdentity(
                Objects.toString(principal.getId())
        );
        enterpriseService.applyActiveMetadata(user, details);
        return details;
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
