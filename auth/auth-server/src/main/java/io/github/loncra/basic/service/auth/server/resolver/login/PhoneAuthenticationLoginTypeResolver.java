package io.github.loncra.basic.service.auth.server.resolver.login;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.PhoneNumberAuthConfig;
import io.github.loncra.basic.service.auth.server.domain.PhoneNumberPrincipal;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.auth.server.resolver.LoginTypeResolver;
import io.github.loncra.basic.service.auth.server.resolver.PhoneNumberAuthenticationResolver;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 手机号码一键登录类型解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class PhoneAuthenticationLoginTypeResolver implements LoginTypeResolver {

    private final List<PhoneNumberAuthenticationResolver> phoneNumberAuthenticationResolvers;

    private final PhoneNumberAuthConfig phoneNumberAuthConfig;

    @Override
    public boolean isSupport(String loginType) {
        return LoginTypeEnum.PHONE_AUTHENTICATION.toString().equals(loginType);
    }

    @Override
    public String getUsername(RequestAuthenticationToken token) {
        String channel = token.getParameterMap().getFirst(phoneNumberAuthConfig.getChannelParamName());
        PhoneNumberAuthenticationResolver resolver = phoneNumberAuthenticationResolvers
                .stream()
                .filter(s -> s.getType().getValue().equals(channel))
                .findFirst().orElseThrow(() -> new SystemException("找不到 [" + channel + "] 的手机号码一键登录支持"));
        String phoneNumber = resolver.getPhoneNumber(token.getParameterMap());
        token.getParameterMap().add(phoneNumberAuthConfig.getPhoneNumberParamName(), phoneNumber);
        return phoneNumber;
    }

    @Override
    public Boolean matchesPassword(
            String presentedPassword,
            RequestAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        String channel = token.getParameterMap()
                .getFirst(phoneNumberAuthConfig.getChannelParamName());

        PhoneNumberAuthenticationResolver resolver = phoneNumberAuthenticationResolvers
                .stream()
                .filter(s -> s.getType().getValue().equals(channel))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到渠道为 [" + channel + "] 的手机号码一键登录实现"));
        return resolver.verifyPhoneNumber(token.getParameterMap());
    }

    @Override
    public <T extends AbstractBasicSystemUser> void preInsertUser(
            RequestAuthenticationToken token,
            T user
    ) {
        if (user instanceof PhoneNumberPrincipal phoneNumberPrincipal) {
            String phoneNumber = token.getParameterMap()
                    .getFirst(phoneNumberAuthConfig.getPhoneNumberParamName());
            if (StringUtils.isNotEmpty(phoneNumber)) {
                phoneNumberPrincipal.setPhoneNumber(phoneNumber);
                phoneNumberPrincipal.setPhoneNumberVerified(YesOrNo.Yes);
            }
        }
    }
}
