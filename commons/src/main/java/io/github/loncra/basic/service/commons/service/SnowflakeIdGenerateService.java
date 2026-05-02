package io.github.loncra.basic.service.commons.service;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.Service;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeIdGenerator;
import io.github.loncra.framework.commons.generator.twitter.SnowflakeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author maurice.chen
 */
@Slf4j
@RequiredArgsConstructor
public class SnowflakeIdGenerateService {

    public static final String DEFAULT_APPLICATION_NAME_CONFIG = "spring.application.name";

    private final SnowflakeProperties properties;

    private final NacosDiscoveryProperties discoveryProperties;

    private final NacosServiceManager nacosServiceManager;

    private final Environment env;

    private SnowflakeIdGenerator snowflakeIdGenerator;

    public String generateSnowflakeId() {
        return snowflakeIdGenerator.generateId();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        SystemException.convertRunnable(this::installSnowflakeWorkIdIfNull, "初始化雪花 workId 出错");
    }

    private void installSnowflakeWorkIdIfNull() throws Exception {
        if (properties.getWorkerId() > 0) {
            return;
        }

        NamingService namingService = nacosServiceManager.getNamingService();

        NamingMaintainService namingMaintainService = nacosServiceManager.getNamingMaintainService(
                discoveryProperties.getNacosProperties()
        );
        // 获取所有服务
        ListView<String> view = namingService.getServicesOfServer(1, Integer.MAX_VALUE, discoveryProperties.getGroup());

        if (log.isDebugEnabled()) {
            log.debug("当前 nacos 组 [{}] 中的所有服务为 {}", discoveryProperties.getGroup(), view.getData());
        }
        String applicationName = env.getProperty(DEFAULT_APPLICATION_NAME_CONFIG);
        String serviceName = view
                .getData()
                .stream()
                .filter(s -> s.equals(applicationName))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到名称为: " + applicationName + " 的应用"));

        Service service = namingMaintainService.queryService(serviceName, discoveryProperties.getGroup());
        Date lastSearchTime = null;
        do {
            if (Objects.nonNull(lastSearchTime) && System.currentTimeMillis() < lastSearchTime.getTime()) {
                continue;
            }
            // 通过服务名获取所有服务实例
            List<Instance> instanceList = namingService.getAllInstances(service.getName(), service.getGroupName());
            if (CollectionUtils.isEmpty(instanceList)) {
                log.info("找不到 {} 的实例信息", service.getName());
                lastSearchTime = Date.from(LocalDateTime.now()
                                                   .plusSeconds(5)
                                                   .atZone(ZoneId.systemDefault())
                                                   .toInstant());
                continue;
            }
            List<String> ips = SystemException.convertSupplier(this::getIps);
            Instance instance = instanceList
                    .stream()
                    .filter(i -> ips.contains(i.getIp()))
                    .findFirst()
                    .orElseThrow(() -> new SystemException("找不到 IP 为: " + ips + " 的服务实例"));
            int index = instanceList.indexOf(instance);
            properties.setWorkerId(index + 1);
            snowflakeIdGenerator = new SnowflakeIdGenerator(properties);
            break;
        } while (true);
    }

    private List<String> getIps() throws SocketException {
        List<String> result = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            // 过滤回环接口、未启用的接口、虚拟网卡
            if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual()) {
                continue;
            }
            // 获取该网卡的所有 IP 地址
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                // 过滤链路本地地址（如 169.254.x.x）和回环地址
                if (inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress()) {
                    continue;
                }
                result.add(inetAddress.getHostAddress());
            }
        }
        return result;
    }

}
