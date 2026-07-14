package io.github.loncra.basic.service.message.server.config;

import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.basic.service.resource.api.service.ResourceCaptchaVerificationService;
import io.github.loncra.framework.captcha.CaptchaProperties;
import io.github.loncra.framework.captcha.storage.support.RedissonCaptchaStorageManager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode",havingValue = "MICROSERVICE")
public class MessageAppStartupAutoConfig {

    @Bean
    public ResourceCaptchaVerificationService captchaVerificationService(CaptchaServiceClient captchaServiceClient) {
        return new ResourceCaptchaVerificationService(captchaServiceClient);
    }

    @Bean
    public RedissonCaptchaStorageManager captchaStorageManager(
            RedissonClient redissonClient,
            CaptchaProperties captchaProperties
    ) {
        return new RedissonCaptchaStorageManager(redissonClient, captchaProperties);
    }
}
