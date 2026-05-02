package io.github.loncra.basic.service.auth.server.config;

import io.github.loncra.basic.service.commons.resolver.soupprt.WeChatAppletShardResolver;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.basic.service.resource.api.service.ResourceCaptchaVerificationService;
import io.github.loncra.framework.captcha.CaptchaProperties;
import io.github.loncra.framework.captcha.storage.support.RedissonCaptchaStorageManager;
import io.github.loncra.framework.spring.security.core.SpringSecurityAutoConfiguration;
import io.github.loncra.framework.wechat.service.WechatAppletService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务配置
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode",havingValue = "MICROSERVICE")
@AutoConfigureAfter(SpringSecurityAutoConfiguration.class)
public class AuthStartupAutoConfig {

    @Bean
    @ConditionalOnBean(FeignClientsConfiguration.class)
    public ResourceCaptchaVerificationService captchaVerificationService(CaptchaServiceClient captchaServiceClient) {
        return new ResourceCaptchaVerificationService(captchaServiceClient);
    }

    @Bean
    @ConditionalOnMissingBean(RedissonCaptchaStorageManager.class)
    public RedissonCaptchaStorageManager captchaStorageManager(
            RedissonClient redissonClient,
            CaptchaProperties captchaProperties
    ) {
        return new RedissonCaptchaStorageManager(redissonClient, captchaProperties);
    }

    @Bean
    @ConditionalOnBean(WechatAppletService.class)
    public WeChatAppletShardResolver weChatAppletShardResolver(WechatAppletService wechatAppletService) {
        return new WeChatAppletShardResolver(wechatAppletService);
    }

}
