package io.github.loncra.basic.service.auth.server.service.user.console;

import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.security.AbstractSystemUserDetailsService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
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
    public List<String> getType() {
        return List.of(ResourceSourceEnum.CONSOLE_SOURCE_VALUE);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return consoleUserService.getPasswordEncoder();
    }

}
