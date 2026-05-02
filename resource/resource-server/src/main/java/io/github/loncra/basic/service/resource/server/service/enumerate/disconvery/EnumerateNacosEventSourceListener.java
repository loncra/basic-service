package io.github.loncra.basic.service.resource.server.service.enumerate.disconvery;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.nacos.event.NacosInstancesChangeEvent;
import io.github.loncra.framework.nacos.event.NacosServiceSubscribeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 插件的 nacos 事件源监听实现，用于把所有微服务带有插件的数据加载后形成权限资源使用。
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(NacosDiscoverySystemResourceService.class)
@ConditionalOnProperty(prefix ="spring.cloud.nacos.discovery", value = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode", havingValue = "MICROSERVICE")
public class EnumerateNacosEventSourceListener {

    private final NacosDiscoverySystemResourceService nacosDiscoverySystemResourceService;

    /**
     * 监听 nacos 服务被订阅事件，自动同步插件資源
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosServiceSubscribeEvent(NacosServiceSubscribeEvent event) {
        NamingEvent namingEvent = CastUtils.cast(event.getSource());
        nacosDiscoverySystemResourceService.syncEnumerateResource(namingEvent);

    }

    /**
     * 监听 nasoc 服务变化事件
     *
     * @param event 事件原型
     */
    @EventListener
    public void onNacosInstancesChangeEvent(NacosInstancesChangeEvent event) {
        NamingEvent namingChangeEvent = CastUtils.cast(event.getSource());

        if (CollectionUtils.isEmpty(namingChangeEvent.getInstances())) {
            Map<String, Map<String, Map<String, Object>>> data = nacosDiscoverySystemResourceService.getServiceEnumerate();
            data.remove(namingChangeEvent.getServiceName());
            return;
        }

        nacosDiscoverySystemResourceService.syncEnumerateResource(namingChangeEvent);
    }
}
