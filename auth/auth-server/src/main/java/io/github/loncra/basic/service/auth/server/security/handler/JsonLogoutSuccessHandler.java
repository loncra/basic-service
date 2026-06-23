package io.github.loncra.basic.service.auth.server.security.handler;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.service.RedissonCacheAuthorizationService;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.FrontEndSystemErrorCodeConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AnonymousUser;
import io.github.loncra.framework.spring.web.device.DeviceUtils;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * json 形式的登出成功具柄实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class JsonLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuthAppConfig authAppConfig;

    private final CommonsConfig commonsConfig;

    private final CaptchaAuthenticationFailureResponse failureHandler;

    private final RedissonCacheAuthorizationService<AbstractBasicSystemUser> redissonCacheAuthorizationService;

    private final ObjectProvider<RememberMeServices> rememberMeServices;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        HttpStatus httpStatus = SpringMvcUtils.getHttpStatus(response);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (Objects.nonNull(authentication) && authentication instanceof AuditAuthenticationToken token) {
            redissonCacheAuthorizationService.deleteSystemUserAllCache(token.getName());
        }

        rememberMeServices.ifAvailable(services -> services.loginFail(request, response));

        RestResult<Map<String, Object>> result = new RestResult<>(
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                new LinkedHashMap<>()
        );

        /*String traceId = request.getHeader(TLogConstants.TLOG_TRACE_KEY);
        if (StringUtils.isNotEmpty(traceId)) {
            result.getMeta().put(SystemConstants.TRACE_ID_FIELD_NAME, traceId);
        }*/
        String json = CastUtils.getObjectMapper()
                .writeValueAsString(result);

        response.getWriter()
                .write(json);
    }

    /**
     * 构造未授权 reset 结果集，目的为乱搞一通，让别人不知道这个是什么。
     *
     * @param request 请求对象
     *
     * @return rest 结果集
     */
    public RestResult<Map<String, Object>> createUnauthorizedResult(HttpServletRequest request) {

        RestResult<Map<String, Object>> result = createRestResult(request);
        postCaptchaData(result, request);

        String deviceId = request.getParameter(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
        }
        if (StringUtils.isEmpty(deviceId)) {
            deviceId = Objects.toString(UUID.randomUUID());
        }

        result.getData()
                .put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, deviceId);
        result.getData()
                .put(SystemConstants.RUNTIME_MODE_KEY, commonsConfig.getRuntimeMode());

        return result;
    }

    /**
     * 创建 reset 结果集
     *
     * @return reset 结果集
     */
    private RestResult<Map<String, Object>> createRestResult(HttpServletRequest request) {

        String executeCode = String.valueOf(HttpStatus.OK.value());
        String message = HttpStatus.OK.getReasonPhrase();
        int status = HttpStatus.OK.value();

        Map<String, Object> data = new LinkedHashMap<>();

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass()) || AnonymousUser.class.isAssignableFrom(authentication.getDetails().getClass())) {
            data.put(SystemConstants.AUTHENTICATED_FIELD_NAME, false);
            message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }
        else {
            data = CastUtils.convertValue(authentication, CastUtils.MAP_TYPE_REFERENCE);
            if (!authentication.isAuthenticated()) {
                message = HttpStatus.UNAUTHORIZED.getReasonPhrase();
            }
        }

        String identified = Objects.toString(
                request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME),
                Objects.toString(UUID.randomUUID())
        );

        data.put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, identified);

        RestResult<Object> result = new RestResult<>(
                message,
                status,
                executeCode,
                data
        );

        return RestResult.of(message, status, executeCode, CastUtils.cast(result.getData()));
    }

    /**
     * 验证码数据处理
     *
     * @param result  reset 结果集
     * @param request http 请求信息
     */
    private void postCaptchaData(
            RestResult<Map<String, Object>> result,
            HttpServletRequest request
    ) {

        IdValueMetadata<String, Map<String, Object>> meta = failureHandler.getAllowableFailureMeta(request);
        String numberString = Objects.toString(
                meta.getValue().get(CaptchaAuthenticationFailureResponse.ALLOWABLE_FAILURE_NUMBER_NAME),
                String.valueOf(BigDecimal.ZERO.intValue())
        );
        Integer number = CastUtils.cast(numberString, Integer.class);

        Integer allowableFailureNumber = authAppConfig.getAllowableFailureNumber();

        if (number >= allowableFailureNumber) {
            String captchaType = authAppConfig.getFormLoginFailureCaptchaType();

            Map<String, Object> buildToken = failureHandler.getCaptchaServiceClient()
                    .createCaptchaToken(captchaType, meta.getId(), new LinkedHashMap<>());

            result.getData()
                    .put(SystemConstants.CAPTCHA_TOKEN_NAME, buildToken);
            result.setExecuteCode(FrontEndSystemErrorCodeConstants.CAPTCHA_EXECUTE_CODE);
        }
    }
}
