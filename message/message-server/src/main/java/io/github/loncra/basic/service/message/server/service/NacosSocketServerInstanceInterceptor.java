package io.github.loncra.basic.service.message.server.service;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingService;
import io.github.loncra.basic.service.message.server.config.MessageAppConfig;
import io.github.loncra.framework.socketio.core.SocketProperties;
import io.github.loncra.framework.socketio.core.interceptor.SocketServerInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * nacos socket 服务实例注册拦截器
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix ="spring.cloud.nacos.discovery", value = "enabled", matchIfMissing = true)
public class NacosSocketServerInstanceInterceptor implements SocketServerInterceptor {

    private final NacosServiceManager nacosServiceManager;

    private final NacosDiscoveryProperties discoveryProperties;

    private final SocketProperties socketConfig;

    private final MessageAppConfig messageAppConfig;

    @Override
    public void destroy() throws Exception{
        NamingService naming = nacosServiceManager.getNamingService();

        naming.deregisterInstance(
                messageAppConfig.getSocketServerNacosInstanceName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                socketConfig.getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "从 nacos 关闭 链接 socket 服务 {}，端口为: {}",
                messageAppConfig.getSocketServerNacosInstanceName(),
                socketConfig.getPort()
        );
    }

    @Override
    public void run(String... args) throws Exception{
        log.info("已启动 socket 服务，端口为:{}", socketConfig.getPort());

        NamingService naming = nacosServiceManager.getNamingService();

        naming.registerInstance(
                messageAppConfig.getSocketServerNacosInstanceName(),
                discoveryProperties.getGroup(),
                discoveryProperties.getIp(),
                socketConfig.getPort(),
                Constants.DEFAULT_CLUSTER_NAME
        );

        log.info(
                "注册链接 socket 服务 {} 到 nacos，端口为: {}",
                messageAppConfig.getSocketServerNacosInstanceName(),
                socketConfig.getPort()
        );
    }
}
