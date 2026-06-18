package io.github.loncra.basic.service.auth.server.service.user.console;

import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.resolver.login.LoadDatabaseLoginTypeResolver;
import io.github.loncra.basic.service.auth.server.security.AbstractSystemUserDetailsService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 系统用户明细认证授权服务实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class ConsoleUserDetailsService extends AbstractSystemUserDetailsService<ConsoleUserEntity> {

    private final ConsoleUserService consoleUserService;

    private final LoadDatabaseLoginTypeResolver loadDatabaseLoginTypeResolver;

    @Override
    protected ConsoleUserEntity getByIdentity(String id) {
        return consoleUserService.getByIdentity(id);
    }

    @Override
    protected void updateLastAuthenticationTime(
            Object id,
            Instant date
    ) {
        consoleUserService
                .lambdaUpdate()
                .set(AbstractPlatformUser::getLastAuthenticationTime, date)
                .eq(AbstractPlatformUser::getId, id)
                .update();
    }

    @Override
    public boolean matchesPassword(
            String presentedPassword,
            TypeAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        boolean result = super.matchesPassword(presentedPassword, token, principal);

        if (!result) {
            RequestAuthenticationToken requestAuthenticationToken = CastUtils.cast(token);
            return loadDatabaseLoginTypeResolver.matchesPassword(presentedPassword, requestAuthenticationToken, principal);
        }
        return true;

    }

    @Override
    public List<String> getType() {
        return List.of(ResourceSourceEnum.CONSOLE_SOURCE_VALUE);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return consoleUserService.getPasswordEncoder();
    }

}
