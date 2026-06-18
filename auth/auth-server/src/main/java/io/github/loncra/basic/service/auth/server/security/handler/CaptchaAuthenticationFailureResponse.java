package io.github.loncra.basic.service.auth.server.security.handler;

import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.commons.constants.FrontEndSystemErrorCodeConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.framework.captcha.token.BuildToken;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.spring.security.core.authentication.config.AuthenticationProperties;
import io.github.loncra.framework.spring.security.core.authentication.handler.JsonAuthenticationFailureResponse;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class CaptchaAuthenticationFailureResponse implements JsonAuthenticationFailureResponse {

    public static final String ALLOWABLE_FAILURE_NUMBER_NAME = "failureNumber";

    @Getter
    private final AuthAppConfig authAppConfig;

    @Getter
    private final AuthenticationProperties authenticationProperties;

    @Getter
    private final CaptchaServiceClient captchaServiceClient;

    private final RedissonClient redissonClient;

    @Override
    public void setting(
            RestResult<Map<String, Object>> result,
            HttpServletRequest request,
            AuthenticationException e
    ) {

        if (e instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException exception = CastUtils.cast(e);
            result.setExecuteCode(exception.getError()
                                          .getErrorCode());
            return;
        }

        Map<String, Object> data = result.getData();

        /*String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        if (StringUtils.isNotEmpty(traceId)) {
            result.getMeta().put(SystemConstants.TRACE_ID_FIELD_NAME, traceId);
        }*/

        if (AuthenticationCredentialsNotFoundException.class.isAssignableFrom(e.getClass())) {
            result.setExecuteCode(FrontEndSystemErrorCodeConstants.LOGIN_EXECUTE_CODE);
            return;
        }

        IdValueMetadata<String, Map<String, Object>> meta = getAllowableFailureMeta(request);
        // 获取错误次数
        Integer number = CastUtils.cast(
                Objects.toString(meta.getValue().get(ALLOWABLE_FAILURE_NUMBER_NAME), String.valueOf(BigDecimal.ZERO.intValue())),
                Integer.class
        );

        String type = request.getParameter(AuthAppConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);

        LoginTypeEnum loginType = NameEnum.ofEnum(LoginTypeEnum.class, type);

        if (LoginTypeEnum.USERNAME_PASSWORD.equals(loginType)) {
            data.put(ALLOWABLE_FAILURE_NUMBER_NAME, ++number);
        }

        if (number < authAppConfig.getAllowableFailureNumber()) {
            meta.setValue(data);
            saveAllowableFailureMeta(meta);
            return;
        }

        // 获取设备唯一识别
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        Map<String, Object> buildToken = captchaServiceClient.createCaptchaToken(
                authAppConfig.getFormLoginFailureCaptchaType(),
                identified,
                new LinkedHashMap<>()
        );

        data.put(SystemConstants.CAPTCHA_TOKEN_NAME, buildToken);

        result.setExecuteCode(FrontEndSystemErrorCodeConstants.CAPTCHA_EXECUTE_CODE);

        meta.setValue(data);
        saveAllowableFailureMeta(meta);

    }

    private void saveAllowableFailureMeta(IdValueMetadata<String, Map<String, Object>> meta) {
        CacheProperties cache = authAppConfig.getAllowableFailureNumberCache();
        String key = cache.getName(meta.getId());
        RBucket<IdValueMetadata<String, Map<String, Object>>> bucket = redissonClient.getBucket(key);
        TimeProperties expiresTime = cache.getExpiresTime();

        if (Objects.isNull(expiresTime)) {
            bucket.setAsync(meta);
        }
        else {
            bucket.setAsync(meta, expiresTime.getValue(), expiresTime.getUnit());
        }
    }

    /**
     * 是否需要验证码认证
     *
     * @param request 请求信息
     * @return true 是，否则 false
     */
    /*public boolean isCaptchaAuthentication(HttpServletRequest request) {
        String type = request.getParameter(ApplicationConfig.DEFAULT_LOGIN_TYPE_PARAM_NAME);
        AuthenticationTypeEnum loginType = NameEnumUtils.parse(type, AuthenticationTypeEnum.class, true);
        if (!AuthenticationTypeEnum.USERNAME_PASSWORD.equals(loginType)) {
            return false;
        }

        IdValueMetadata<String, Map<String, Object>> meta = getAllowableFailureMeta(request);
        Integer number = CastUtils.cast(
                Objects.toString(meta.getValue().get(ALLOWABLE_FAILURE_NUMBER_NAME), String.valueOf(BigDecimal.ZERO.intValue())),
                Integer.class
        );

        return number >= applicationConfig.getAllowableFailureNumber();
    }*/

    /**
     * 删除允许认证失败次数
     *
     * @param request 请求信息
     */
    public void deleteAllowableFailureNumber(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);

        String key = authAppConfig.getAllowableFailureNumberCache()
                .getName(identified);

        redissonClient.getBucket(key).deleteAsync();
    }

    public IdValueMetadata<String, Map<String, Object>> getAllowableFailureMeta(HttpServletRequest request) {
        String identified = SpringMvcUtils.getDeviceIdentified(request);
        String key = authAppConfig.getAllowableFailureNumberCache()
                .getName(identified);
        RBucket<IdValueMetadata<String, Map<String, Object>>> bucket = redissonClient.getBucket(key);

        IdValueMetadata<String, Map<String, Object>> result = new IdValueMetadata<>();
        result.setId(identified);
        result.setValue(new LinkedHashMap<>());
        if (bucket.isExists()) {
            result = bucket.get();
        }

        return result;
    }

}
