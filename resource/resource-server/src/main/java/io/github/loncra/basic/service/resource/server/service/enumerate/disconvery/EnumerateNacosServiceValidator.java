package io.github.loncra.basic.service.resource.server.service.enumerate.disconvery;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import io.github.loncra.basic.service.resource.server.config.ResourceAppConfig;
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
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode", havingValue = "MICROSERVICE")
@ConditionalOnProperty(prefix ="spring.cloud.nacos.discovery", value = "enabled", matchIfMissing = true)
public class EnumerateNacosServiceValidator implements NacosServiceListenerValidator {

    private final ResourceAppConfig resourceAppConfig;

    @Override
    public boolean isSupport(NamingEvent nacosService) {
        return true;
    }

    @Override
    public boolean subscribeValid(NamingEvent nacosService) {
        return !resourceAppConfig.getIgnoreEnumerateService()
                .contains(nacosService.getServiceName());
    }
}
