package io.github.loncra.basic.service.auth.server.security.handler;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.PluginResourceService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.handler.JsonAuthenticationSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix ="spring.cloud.discovery", value = "enabled", havingValue = "true")
public class DiscoverySuccessResponse implements JsonAuthenticationSuccessResponse {

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    private final DiscoveryClient discoveryClient;

    private final PluginResourceService pluginResourceService;

    /**
     * 当前运行的服务信息
     */
    private final static String DEFAULT_SERVICES_NAME = "services";

    private final static String DEFAULT_VERSION_NAME = "version";

    /**
     * 当前插件的服务信息
     */
    private final static String DEFAULT_PLUGIN_NAME = "pluginServices";

    @Override
    public void setting(
            RestResult<Object> result,
            HttpServletRequest request
    ) {


        result.getMetadata()
                .put(DEFAULT_VERSION_NAME, nacosDiscoveryProperties.getMetadata().get(DEFAULT_VERSION_NAME));
        result.getMetadata()
                .put(DEFAULT_SERVICES_NAME, discoveryClient.getServices());
        result.getMetadata()
                .put(DEFAULT_PLUGIN_NAME, pluginResourceService.getPluginServerNames());
    }
}
