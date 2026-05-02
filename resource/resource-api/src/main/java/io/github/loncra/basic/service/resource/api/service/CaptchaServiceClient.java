package io.github.loncra.basic.service.resource.api.service;

import io.github.loncra.framework.captcha.token.BuildToken;
import io.github.loncra.framework.captcha.token.InterceptToken;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.config.CaptchaVerificationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证码服务客户端
 *
 * @author maurice.chen
 */
public interface CaptchaServiceClient {


    String PHONE_NUMBER_PARAM_NAME = "phoneNumberParamName";

    String EMAIL_PARAM_NAME = "emailParamName";

    String SMS_CAPTCHA_PARAM_NAME = "_smsCaptcha";

    String SMS_CHANNEL_PARAM_NAME = "channelParamName";

    String EMAIL_CAPTCHA_PARAM_NAME = "_emailCaptcha";

    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     *
     * @return 绑定 token
     */
    InterceptToken createCaptchaIntercept(
            String token,
            String type,
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
    BuildToken createCaptchaToken(
            String type,
            String deviceIdentified,
            Map<String, Object> appendParams
    );

    /**
     * 生成验证码
     *
     * @param param 验证码 token 信息
     *
     * @return 生成结果
     */
    Object generateCaptcha(
            Map<String, Object> param
    );

    /**
     * 校验验证码
     *
     * @param param 参数信息
     *
     * @return rest 结果集
     */
    RestResult<Object> verifyCaptcha(
            Map<String, Object> param
    );

    /**
     * 删除验证码
     *
     * @param param 参数信息
     *
     * @return rest 结果集
     */
    RestResult<Object> deleteCaptcha(
            Map<String, Object> param
    );

    /**
     * 删除验证码
     *
     * @param captchaType  验证码类型
     * @param captchaToken 验证码 token
     * @param paramName    验证码token 参数
     *
     * @return rest 结果集
     */
    default RestResult<Object> deleteCaptcha(
            String captchaType,
            String captchaToken,
            String paramName
    ) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put(CaptchaVerificationProperties.DEFAULT_CAPTCHA_TYPE_PARAM_NAME, captchaType);
        param.put(paramName, captchaToken);

        return deleteCaptcha(param);
    }
}
