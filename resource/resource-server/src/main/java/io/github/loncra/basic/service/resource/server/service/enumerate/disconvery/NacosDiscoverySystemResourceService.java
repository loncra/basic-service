package io.github.loncra.basic.service.resource.server.service.enumerate.disconvery;


import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.server.service.enumerate.AbstractRedissonSystemResourceService;
import io.github.loncra.basic.service.resource.server.service.enumerate.scan.ScanModuleSystemResourceService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.nacos.event.NacosSpringEventManager;
import io.github.loncra.framework.security.plugin.PluginInfo;
import io.github.loncra.framework.spring.security.core.authentication.config.AuthenticationProperties;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import io.github.loncra.framework.spring.web.endpoint.EnumerateEndpoint;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 服务发现形式的枚举资源服务实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
@AutoConfigureBefore(ScanModuleSystemResourceService.class)
@ConditionalOnProperty(prefix ="spring.cloud.nacos.discovery", value = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode", havingValue = "MICROSERVICE")
public class NacosDiscoverySystemResourceService extends AbstractRedissonSystemResourceService {

    private static final Map<String, Instance> INSTANCE_CACHE = new LinkedHashMap<>();

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_ENUMERATE_INFO_URL = "/actuator/enumerate";

    private final RestTemplate restTemplate;

    private final NacosSpringEventManager nacosSpringEventManager;

    private final AuthenticationProperties authenticationProperties;

    /**
     * 匹配最大版本实例
     *
     * @param target 目标实例
     * @param source 原实例
     *
     * @return 0 相等，小于0 小于，大于0 大于
     */
    public int comparingInstanceVersion(
            Instance target,
            Instance source
    ) {
        return getInstanceVersion(target).compareTo(getInstanceVersion(source));
    }

    /**
     * 获取实例的版本信息
     *
     * @param instance 实例
     *
     * @return 版本信息
     */
    public Version getInstanceVersion(Instance instance) {

        String version = instance.getMetadata()
                .get(PluginInfo.DEFAULT_VERSION_NAME);
        String groupId = instance.getMetadata()
                .get(PluginInfo.DEFAULT_GROUP_ID_NAME);
        String artifactId = instance.getMetadata()
                .get(PluginInfo.DEFAULT_ARTIFACT_ID_NAME);

        return VersionUtil.parseVersion(version, groupId, artifactId);
    }

    /**
     * 同步枚举資源
     *
     * @param namingEvent 服务变更事件
     */
    public void syncEnumerateResource(
            NamingEvent namingEvent
    ) {
        Optional<Instance> optional = namingEvent.getInstances()
                .stream()
                .max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("找不到服务为 [{}] 的最高版本实例", namingEvent.getServiceName());
            return;
        }

        Instance instance = optional.get();

        Map<String, Object> info = getInstanceEnumerate(instance);

        syncEnumerateCache(namingEvent.getServiceName(), info, instance);

    }

    protected void syncEnumerateCache(
            String serviceName,
            Map<String, Object> info,
            Instance instance
    ) {
        if (MapUtils.isEmpty(info)) {
            return;
        }

        if (!info.containsKey(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME)) {
            return;
        }

        Map<String, Map<String, Object>> enumerateData = CastUtils.cast(info.get(EnumerateEndpoint.DEFAULT_ENUM_KEY_NAME));

        Version version = getInstanceVersion(instance);

        if (INSTANCE_CACHE.containsKey(serviceName)) {
            Instance exist = INSTANCE_CACHE.get(serviceName);

            Version existVersion = getInstanceVersion(exist);

            int compare = existVersion.compareTo(version);
            if (compare > 0) {
                return;
            }
            else if (compare == 0) {
                // 版本号相同，比较内容哈希
                Map<String, Map<String, Object>> existData = getServiceEnumerate().get(serviceName);
                String existHash = buildHash(existData);
                String newHash = buildHash(enumerateData);
                if (Objects.equals(existHash, newHash)) {
                    return;
                }
            }
        }

        getServiceEnumerate().put(serviceName, enumerateData);

        INSTANCE_CACHE.put(serviceName, instance);
    }

    /**
     * 获取实例枚举信息
     *
     * @param instance 实例
     *
     * @return 实例信息
     */
    public Map<String, Object> getInstanceEnumerate(Instance instance) {

        String http = Strings.CS.prependIfMissing(instance.toInetAddr(), SpringMvcUtils.HTTP_PROTOCOL_PREFIX);
        String url = Strings.CS.appendIfMissing(http, DEFAULT_ENUMERATE_INFO_URL);
        HttpHeaders headers = FeignAuthenticationConfiguration.of(authenticationProperties);
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, headers), SystemConstants.MAP_REFERENCE)
                .getBody();
    }

    /**
     * 同步所有枚举資源
     */
    @Override
    public void syncEnumerate() throws Exception {
        nacosSpringEventManager.destroy();
        nacosSpringEventManager.subscribeService();
    }
}
