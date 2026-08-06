package io.github.loncra.basic.service.ai.server.config;

import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.sql.Time;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.ai.app.stream")
public class StreamConfig {

    private TimeProperties pollInterval;

    private TimeProperties delayedExecutorCleanTime = TimeProperties.ofSeconds(2);

    private TimeProperties interruptTimeToLive = TimeProperties.ofSeconds(30);

    /*@Deprecated
    private TimeProperties removeExpireTime = TimeProperties.ofSeconds(1);*/
}
