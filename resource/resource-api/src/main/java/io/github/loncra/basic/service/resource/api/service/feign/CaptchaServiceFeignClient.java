package io.github.loncra.basic.service.resource.api.service.feign;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.framework.captcha.token.BuildToken;
import io.github.loncra.framework.captcha.token.InterceptToken;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 验证码服务客户端
 *
 * @author maurice.chen
 */
@ConditionalOnClass(FeignClientsConfiguration.class)
@FeignClient(name = SystemConstants.SYS_RESOURCE_NAME, contextId = "captchaServiceClient", configuration = FeignAuthenticationConfiguration.class)
public interface CaptchaServiceFeignClient extends CaptchaServiceClient {

    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     *
     * @return 绑定 token
     */
    @Override
    @PostMapping("captcha/createCaptchaIntercept")
    InterceptToken createCaptchaIntercept(
            @RequestParam("token")
            String token,
            @RequestParam("type")
            String type,
            @RequestParam("interceptType")
            String interceptType
    );

    /**
     * 创建验证码绑定 token
     *
     * @param type             验证码类型
     * @param deviceIdentified 唯一识别
     * @param appendParams     附加参数
     *
     * @return 绑定 token
     */
    @Override
    @GetMapping("captcha/generateToken")
    BuildToken createCaptchaToken(
            @RequestParam("type")
            String type,
            @RequestParam("deviceIdentified")
            String deviceIdentified,
            @RequestParam
            Map<String, Object> appendParams
    );

    /**
     * 生成验证码
     *
     * @param param 验证码 token 信息
     *
     * @return 生成结果
     */
    @Override
    @GetMapping("captcha/generateCaptcha")
    Object generateCaptcha(
            @RequestParam
            Map<String, Object> param
    );

    /**
     * 校验验证码
     *
     * @param param 参数信息
     *
     * @return rest 结果集
     */
    @Override
    @PostMapping("captcha/verifyCaptcha")
    RestResult<Object> verifyCaptcha(
            @RequestParam
            Map<String, Object> param
    );

    /**
     * 删除验证码
     *
     * @param param 参数信息
     *
     * @return rest 结果集
     */
    @Override
    @PostMapping("captcha/deleteCaptcha")
    RestResult<Object> deleteCaptcha(
            @RequestParam
            Map<String, Object> param
    );

}
