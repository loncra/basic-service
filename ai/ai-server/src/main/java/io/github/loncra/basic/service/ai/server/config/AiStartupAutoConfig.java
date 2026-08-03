package io.github.loncra.basic.service.ai.server.config;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.extensions.mysql.state.MysqlAgentStateStore;
import io.github.loncra.basic.service.resource.api.service.CaptchaServiceClient;
import io.github.loncra.basic.service.resource.api.service.ResourceCaptchaVerificationService;
import io.github.loncra.framework.captcha.CaptchaProperties;
import io.github.loncra.framework.captcha.storage.support.RedissonCaptchaStorageManager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 服务配置
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode",havingValue = "MICROSERVICE")
public class AiStartupAutoConfig {

    public static final String AGENT_STATE_STORE_TABLE_NAME = "tb_agent_state_store";

    @Bean
    @ConditionalOnBean(FeignClientsConfiguration.class)
    public ResourceCaptchaVerificationService captchaVerificationService(CaptchaServiceClient resourceServiceFeignClient) {
        return new ResourceCaptchaVerificationService(resourceServiceFeignClient);
    }

    @Bean
    public RedissonCaptchaStorageManager captchaStorageManager(
            RedissonClient redissonClient,
            CaptchaProperties captchaProperties
    ) {
        return new RedissonCaptchaStorageManager(redissonClient, captchaProperties);
    }

    @Bean
    public AgentStateStore agentStateStore(DataSource dataSource) throws SQLException {
        String catalog;
        try (Connection conn = dataSource.getConnection()) {
            catalog = conn.getCatalog();
        }
        return new MysqlAgentStateStore(
                dataSource,
                catalog,
                AGENT_STATE_STORE_TABLE_NAME,
                true
        );
    }

}