package io.github.loncra.basic.service.auth.server.resolver.login;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.config.PhoneNumberAuthConfig;
import io.github.loncra.basic.service.auth.server.domain.PhoneNumberPrincipal;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.auth.server.resolver.LoginTypeResolver;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ErrorCodeException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加载数据库的登录类型解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class LoadDatabaseLoginTypeResolver implements LoginTypeResolver {

    private final CaptchaServiceClient captchaServiceClient;

    private final PhoneNumberAuthConfig phoneNumberAuthConfig;

    @Override
    public boolean isSupport(String loginType) {
        return LoginTypeEnum.LOAD_DATABASE_TYPES.contains(loginType);
    }

    @Override
    public String getUsername(RequestAuthenticationToken token) {
        return token.getPrincipal().toString();
    }

    @Override
    public Boolean matchesPassword(
            String presentedPassword,
            RequestAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        String loginType = token.getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        if (LoginTypeEnum.PHONE_CAPTCHA.toString().equals(loginType)) {
            Map<String, Object> params = createVerifyCaptchaParam(presentedPassword, token);
            RestResult<Object> result = captchaServiceClient.verifyCaptcha(params);
            SystemException.isTrue(result.isSuccess(), () -> new ErrorCodeException(result.getMessage(), result.getExecuteCode()));
            return true;
        } else {
            return null;
        }
    }

    private Map<String, Object> createVerifyCaptchaParam(
            String presentedPassword,
            RequestAuthenticationToken token
    ) {

        Map<String, Object> params = new LinkedHashMap<>();

        params.put(phoneNumberAuthConfig.getSmsCaptchaValueParamName(), presentedPassword);
        params.put(phoneNumberAuthConfig.getSmsCaptchaTokenParamName(), token.getParameterMap()
                .getFirst(phoneNumberAuthConfig.getSmsCaptchaTokenParamName()));
        params.put(PrincipalDetailsConstants.PHONE_NUMBER_KEY, token.getPrincipal()
                .toString());
        params.put(phoneNumberAuthConfig.getSmsCaptchaTypeParamName(), token.getParameterMap()
                .get(phoneNumberAuthConfig.getSmsCaptchaTypeParamName()));

        return params;
    }

    @Override
    public <T extends AbstractBasicSystemUser> void preInsertUser(
            RequestAuthenticationToken token,
            T user
    ) {
        String loginType = token.getParameterMap()
                .getFirst(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        if (LoginTypeEnum.PHONE_CAPTCHA.toString().equals(loginType) && user instanceof PhoneNumberPrincipal phoneNumberPrincipal) {
            phoneNumberPrincipal.setPhoneNumber(token.getPrincipal().toString());
            phoneNumberPrincipal.setPhoneNumberVerified(YesOrNo.Yes);
        }
    }
}
