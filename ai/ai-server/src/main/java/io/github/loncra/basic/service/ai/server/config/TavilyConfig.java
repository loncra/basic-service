package io.github.loncra.basic.service.ai.server.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.ai.app.tavily")
public class TavilyConfig {

    private String baseUrl = "https://mcp.tavily.com/mcp/";
    private String apiKeyField = "tavilyApiKey";
    private String defaultParamsHeaderField = "DEFAULT_PARAMETERS";
    private Map<String, Object> defaultParams = Map.of(
            "include_favicon",true,
            "max_results",5,
            "search_depth","advanced"
    );
}
