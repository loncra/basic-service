package io.github.loncra.basic.service.commons.config;

import io.github.loncra.framework.commons.domain.metadata.CloudSecretMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serial;

/**
 * 阿里云配置
 *
 * @author maurice.chen
 */
@Data
@Slf4j
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("loncra.basic-service.commons.app.alibaba-cloud")
public class AlibabaCloudConfig extends CloudSecretMetadata {

    @Serial
    private static final long serialVersionUID = -6122772449538676869L;

    private String endpoint = "dysmsapi.aliyuncs.com";
}
