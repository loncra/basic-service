package io.github.loncra.basic.service.auth.server.config;

import io.github.loncra.basic.service.auth.server.security.handler.JsonLogoutSuccessHandler;
import io.github.loncra.framework.spring.security.core.authentication.AuditAuthenticationDetailsSource;
import io.github.loncra.framework.spring.security.core.authentication.adapter.WebSecurityConfigurerAfterAdapter;
import io.github.loncra.framework.spring.security.core.authentication.config.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 自定义 spring security 的配置
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class SpringSecurityConfig implements WebSecurityConfigurerAfterAdapter {

    private final AuthAppConfig authAppConfig;

    private final JsonLogoutSuccessHandler jsonLogoutSuccessHandler;

    private final AuthenticationProperties authenticationProperties;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    @Override
    public void configure(HttpSecurity httpSecurity) {

        try {

            httpSecurity
                    .formLogin(form -> form
                            .passwordParameter(authenticationProperties.getPasswordParamName())
                            .usernameParameter(authenticationProperties.getUsernameParamName())
                            .loginProcessingUrl(authenticationProperties.getLoginProcessingUrl())
                            .authenticationDetailsSource(new AuditAuthenticationDetailsSource(authenticationProperties))
                            .failureHandler(authenticationFailureHandler)
                            .successHandler(authenticationSuccessHandler)
                    )
                    .logout(logout -> logout.logoutUrl(authAppConfig.getLogoutUrl())
                            .logoutSuccessHandler(jsonLogoutSuccessHandler));

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
