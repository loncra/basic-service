package io.github.loncra.basic.service.resource.api.service;

import io.github.loncra.framework.captcha.filter.CaptchaVerificationService;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.RestResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 资源服务验证码校验服务实现
 *
 * @author maurice.chen
 */
@Slf4j
@RequiredArgsConstructor
@ConditionalOnClass(FeignClientsConfiguration.class)
public class ResourceCaptchaVerificationService implements CaptchaVerificationService {

    public static final List<String> DEFAULT_TYPE = Arrays.asList("tianai", "email");

    private final CaptchaServiceClient captchaServiceClient;

    @Override
    public List<String> getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public void verify(HttpServletRequest request) {
        Map<String, Object> param = HttpRequestParameterMapUtils.castArrayValueMapToObjectValueMap(request.getParameterMap());
        RestResult<Object> result = captchaServiceClient.verifyCaptcha(param);
        Assert.isTrue(result.getStatus() == HttpStatus.OK.value(), result.getMessage());
    }

    @Override
    public void delete(HttpServletRequest request) {
        try {
            Map<String, Object> param = HttpRequestParameterMapUtils.castArrayValueMapToObjectValueMap(request.getParameterMap());
            captchaServiceClient.deleteCaptcha(param);
        }
        catch (Exception e) {
            log.warn("删除验证码失败", e);
        }
    }
}
