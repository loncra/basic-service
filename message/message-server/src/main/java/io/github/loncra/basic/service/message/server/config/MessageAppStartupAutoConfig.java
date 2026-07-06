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

    /*@Bean
    public RoomServiceClient roomServiceClient(
            UserChatCallConfig userChatCallConfig
    ) {

        return RoomServiceClient.createClient(
                userChatCallConfig.getLivekit().getHost(),
                userChatCallConfig.getLivekit().getSecret().getSecretId(),
                userChatCallConfig.getLivekit().getSecret().getSecretKey()
        );
    }

    @Bean
    public WebhookReceiver webhookReceiver(
            @Value("${loncra.basic-service.message.app.chat.call.live-kit.api-key}")
            String apiKey,
            @Value("${loncra.basic-service.message.app.chat.call.live-kit.secret}")
            String secret
    ){
        return new WebhookReceiver(apiKey, secret);
    }*/

    /*@Bean
    public AccessToken accessToken(
            @Value("${loncra.basic-service.message.app.chat.call.live-kit.api-key}")
            String apiKey,
            @Value("${loncra.basic-service.message.app.chat.call.live-kit.secret}")
            String secret
    ) {
        return new AccessToken(apiKey, secret);
    }*/
}
