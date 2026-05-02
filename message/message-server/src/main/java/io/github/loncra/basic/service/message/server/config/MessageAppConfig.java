package io.github.loncra.basic.service.message.server.config;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
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
@ConfigurationProperties("loncra.basic-service.message.app")
public class MessageAppConfig {

    /**
     * nacos 实例名称
     */
    private String socketServerNacosInstanceName = SystemConstants.SYS_SOCKET_SERVER_NAME;
}
