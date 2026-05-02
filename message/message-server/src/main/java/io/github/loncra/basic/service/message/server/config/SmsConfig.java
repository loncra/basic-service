package io.github.loncra.basic.service.message.server.config;


import io.github.loncra.basic.service.message.api.domian.metadata.SmsConfigPrepareMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;

/**
 * 短信配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "loncra.basic-service.message.app.sms")
public class SmsConfig extends SmsConfigPrepareMetadata {

    @Serial
    private static final long serialVersionUID = 4064395701141428065L;

    /**
     * 渠道商
     */
    private String channel = "alibabaCloud";

    /**
     * 重试次数
     */
    private Integer maxRetryCount = 3;

}
