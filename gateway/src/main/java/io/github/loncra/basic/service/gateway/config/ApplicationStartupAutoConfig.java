package io.github.loncra.basic.service.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loncra.basic.service.gateway.RestResultGatewayBlockExceptionHandler;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.crypto.algorithm.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.Charset;


/**
 * 服务配置
 *
 * @author maurice.chen
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationStartupAutoConfig implements InitializingBean {

    public static final String AUTHENTICATION_SCHEME_BASIC = "Basic ";

    private final ObjectMapper objectMapper;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public RestResultGatewayBlockExceptionHandler restResultGatewayBlockExceptionHandler(
            ObjectMapper objectMapper,
            ApplicationConfig applicationConfig
    ) {
        return new RestResultGatewayBlockExceptionHandler(objectMapper, applicationConfig);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        CastUtils.setObjectMapper(objectMapper);
    }

    @Bean
    public WebClient.Builder mcpWebClientBuilder(ApplicationConfig applicationConfig) {
        String token = applicationConfig.getMcpAuthUser().getName()
                + CacheProperties.DEFAULT_SEPARATOR
                + applicationConfig.getMcpAuthUser().getPassword();

        String value = Base64.encodeToString(token.getBytes(Charset.defaultCharset()));;
        return WebClient.builder().defaultHeader(HttpHeaders.AUTHORIZATION, AUTHENTICATION_SCHEME_BASIC + value);
    }
}
