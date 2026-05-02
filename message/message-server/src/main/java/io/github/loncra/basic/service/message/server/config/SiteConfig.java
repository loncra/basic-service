package io.github.loncra.basic.service.message.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 站内信配置
 */
@Data
@Component
@ConfigurationProperties("loncra.basic-service.message.app.site")
public class SiteConfig {

    /**
     * 渠道商
     */
    private List<String> channel;

    /**
     * 成功状态
     */
    private List<Integer> successStatus = Arrays.asList(HttpStatus.OK.value(), HttpStatus.NOT_FOUND.value());

    /**
     * 重试次数
     */
    private Integer maxRetryCount = 3;
}
