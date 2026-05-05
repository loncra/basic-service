package io.github.loncra.basic.service.auth.server.service.resource.plugin.disconvery;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.framework.nacos.event.NacosServiceListenerValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 插件服务校验
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix ="spring.cloud.nacos.discovery", value = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode", havingValue = "MICROSERVICE")
public class PluginNacosServiceValidator implements NacosServiceListenerValidator {

    private final AuthAppConfig authAppConfig;

    @Override
    public boolean isSupport(NamingEvent nacosService) {
        return true;
    }

    @Override
    public boolean subscribeValid(NamingEvent nacosService) {
        return !authAppConfig.getIgnorePluginService()
                .contains(nacosService.getServiceName());
    }
}
