package io.github.loncra.basic.service.resource.server.config;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CacheProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode
@ConfigurationProperties("loncra.basic-service.resource.app")
public class ResourceAppConfig {

    /**
     * 数据字典缓存
     */
    private CacheProperties dictionaryCache = CacheProperties.of("loncra:basic-service:resource:app:data-dictionary:");

    /**
     * 枚举资源缓存
     */
    private CacheProperties enumerateCache = CacheProperties.of("loncra:basic-service:resource:app:enumerate");


    /**
     * 数据字典分隔符
     */
    private String dictionarySeparator = ".";

    /**
     * 忽略环境变量的开头值
     */
    private List<String> ignoreEnvironmentStartWith = Collections.singletonList("spring");

    /**
     * 忽略的枚举服务集合
     */
    private List<String> ignoreEnumerateService = List.of(SystemConstants.SYS_GATEWAY_NAME, SystemConstants.SYS_SOCKET_SERVER_NAME, "ai-mcp-server::2.0.0-SNAPSHOT", "gateway::2.0.0-SNAPSHOT");

    /**
     * 扫描枚举路径
     */
    private Map<String, List<String>> scanPluginPackages = new LinkedHashMap<>();
}
