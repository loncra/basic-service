package io.github.loncra.basic.service.monolith.config;


import io.github.loncra.framework.commons.generator.twitter.SnowflakeProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.monolith.app")
public class MonolithAppConfig {

    private SnowflakeProperties snowflake;

}
