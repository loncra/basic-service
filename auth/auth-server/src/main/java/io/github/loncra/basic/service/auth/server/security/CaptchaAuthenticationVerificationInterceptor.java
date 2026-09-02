package io.github.loncra.basic.service.auth.server.security;

import io.github.loncra.basic.service.auth.server.security.handler.CaptchaAuthenticationFailureResponse;
import io.github.loncra.basic.service.commons.constants.FrontEndSystemErrorCodeConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.captcha.filter.CaptchaVerificationInterceptor;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CaptchaAuthenticationVerificationInterceptor implements CaptchaVerificationInterceptor {

    private final CaptchaAuthenticationFailureResponse captchaAuthenticationFailureResponse;
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public boolean preVerify(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        IdValueMetadata<String, Map<String, Object>> meta = captchaAuthenticationFailureResponse.getAllowableFailureMetadata(request);
        // 获取错误次数
        Integer number = CastUtils.cast(
                Objects.toString(meta.getValue().get(CaptchaAuthenticationFailureResponse.ALLOWABLE_FAILURE_NUMBER_NAME), String.valueOf(BigDecimal.ZERO.intValue())),
                Integer.class
        );

        return number < captchaAuthenticationFailureResponse.getAuthAppConfig().getAllowableFailureNumber();
    }

    @Override
    public void exceptionVerify(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception e
    ) {
        String url = request.getServletPath();
        if (!matcher.match(DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL, url)) {
            return ;
        }
        String identified = SpringMvcUtils.getDeviceIdentified(request);
        Map<String, Object> buildToken = captchaAuthenticationFailureResponse.getCaptchaServiceClient().createCaptchaToken(
                captchaAuthenticationFailureResponse.getAuthAppConfig().getFormLoginFailureCaptchaType(),
                identified,
                new LinkedHashMap<>()
        );
        RestResult<Map<String, Object>> result = RestResult.ofException(FrontEndSystemErrorCodeConstants.CAPTCHA_EXECUTE_CODE, e);
        result.setData(Map.of(SystemConstants.CAPTCHA_TOKEN_NAME, buildToken));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        SystemException.convertRunnable(() -> response.getWriter().write(CastUtils.getObjectMapper().writeValueAsString(result)), (String) null);
    }
}
