package io.github.loncra.basic.service.resource.server.config.capthca;

import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 短信验证码配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.resource.app.captcha.sms")
public class SmsCaptchaConfig {
    /**
     * 短信验证码的超时时间
     */
    private TimeProperties captchaExpireTime = new TimeProperties(5, TimeUnit.MINUTES);
    /**
     * 提交短信验证码的参数名称
     */
    private String captchaParamName = CaptchaServiceClient.SMS_CAPTCHA_PARAM_NAME;

    /**
     * 短信验证码的随机生成数量
     */
    private Integer randomNumericCount = 6;

    /**
     * 提交手机号码的参数名称
     */
    private String phoneNumberParamName = "phoneNumber";

    /**
     * 拦截器类型
     */
    private String interceptorType;

    /**
     * 验证码重试时间
     */
    private TimeProperties retryTime = TimeProperties.of(60, TimeUnit.SECONDS);
}
