package io.github.loncra.basic.service.ai.server.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.ai.app.tdx")
public class TdxConfig {

    private String baseUrl = "https://mcp.tdx.com.cn:3001/mcp";
    private String apiKeyField = "tdx-api-key";
}
