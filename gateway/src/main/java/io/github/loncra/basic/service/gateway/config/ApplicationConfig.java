package io.github.loncra.basic.service.gateway.config;

import io.github.loncra.framework.commons.exception.ErrorCodeException;
import lombok.Data;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 访问加解密配置
 *
 * @author maurice.chen
 */
@Data
@Component
@ConfigurationProperties(prefix = "loncra.basic-service.gateway")
public class ApplicationConfig {

    /**
     * 当错误获取不到响应的 ReasonPhrase 时，抛出异常的默认信息
     */
    private String defaultReasonPhrase = ErrorCodeException.DEFAULT_ERROR_MESSAGE;

    /**
     * 调用 mcp 时 spring security 授权信息
     */
    private SecurityProperties.User mcpAuthUser = new SecurityProperties.User();
}
