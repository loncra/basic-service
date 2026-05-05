package io.github.loncra.basic.service.auth.server.service.user.personal;

import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.security.AbstractRegistrationSystemUserDetailsService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

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
    protected PersonalUserEntity createOrmSystemUserEntity(
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        PersonalUserEntity user = new PersonalUserEntity();

        user.setUsername(getCommonsConfig().generateRandomUsername(principal.getUsername()));
        user.setNickname(getCommonsConfig().generateRandomNickName());
        user.setPassword(getPasswordEncoder().encode(getCommonsConfig().generateRandomPassword()));
        user.setStatus(UserStatus.Enabled);
        user.setTenantId(snowflakeIdGenerator.generateId());
        //user.setRoleIds(Arrays.stream(PersonalRoleEnum.values()).map(PersonalRoleEnum::getRoleAuthority).toList());

        return user;
    }
}
