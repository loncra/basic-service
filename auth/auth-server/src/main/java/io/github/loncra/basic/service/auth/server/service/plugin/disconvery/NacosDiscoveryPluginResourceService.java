package io.github.loncra.basic.service.auth.server.service.plugin.disconvery;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import io.github.loncra.basic.service.auth.server.domain.dto.DisabledApplicationResourceDto;
import io.github.loncra.basic.service.auth.server.domain.dto.NacosSyncPluginResourceDto;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.service.plugin.AbstractRedissonPluginResourceService;
import io.github.loncra.basic.service.auth.server.service.plugin.scan.ScanModulePluginResourceService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.nacos.event.NacosSpringEventManager;
import io.github.loncra.framework.security.plugin.PluginInfo;
import io.github.loncra.framework.spring.security.core.authentication.config.AuthenticationProperties;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import io.github.loncra.framework.spring.security.core.plugin.PluginEndpoint;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.redisson.api.RList;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件資源管理
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
@AutoConfigureBefore(ScanModulePluginResourceService.class)
@ConditionalOnProperty(prefix ="spring.cloud.nacos.discovery", value = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = "loncra.basic-service.commons.app",value = "runtime-mode", havingValue = "MICROSERVICE")
public class NacosDiscoveryPluginResourceService extends AbstractRedissonPluginResourceService {

    /**
     * 默认获取应用信息的后缀 uri
     */
    private static final String DEFAULT_PLUGIN_INFO_URL = "/actuator/plugin";

    private final RestTemplate restTemplate;

    private final AuthenticationProperties authenticationProperties;

    /**
     * 服务实例缓存，用于记录当前的插件信息是否需要更新资源
     */
    private final Map<String, List<NacosPluginInstance>> instanceCache = new LinkedHashMap<>();

    private final NacosSpringEventManager nacosSpringEventManager;

    /**
     * 重新订阅所有服务
     */
    @Override
    public void resubscribeAllService() throws Exception {
        nacosSpringEventManager.destroy();
        nacosSpringEventManager.subscribeService();
    }

    /**
     * 获取实例 info
     *
     * @param instance 实例
     *
     * @return 实例信息
     */
    public Map<String, Object> getInstanceInfo(Instance instance) {
        String http = Strings.CS.prependIfMissing(instance.toInetAddr(), SpringMvcUtils.HTTP_PROTOCOL_PREFIX);
        String url = Strings.CS.appendIfMissing(http, DEFAULT_PLUGIN_INFO_URL);
        HttpHeaders httpHeaders = FeignAuthenticationConfiguration.of(authenticationProperties);
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, httpHeaders), SystemConstants.MAP_REFERENCE)
                .getBody();
    }

    /**
     * 匹配最大版本实例
     *
     * @param target 目标实例
     * @param source 原实例
     *
     * @return 0 相等，小于 0 小于，大于 0 大于
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

    public NacosSyncPluginResourceDto syncPluginResource(
            NamingEvent namingEvent
    ) {

        Optional<Instance> optional = namingEvent.getInstances().stream()
                .max(this::comparingInstanceVersion);

        if (optional.isEmpty()) {
            log.warn("找不到服务为 [{}][{}] 的最高版本实例", namingEvent.getGroupName(), namingEvent.getServiceName());
            return new NacosSyncPluginResourceDto();
        }

        Instance instance = optional.get();
        // 获取实例版本信息
        Version version = getInstanceVersion(instance);

        NacosPluginInstance nacosPluginInstance = CastUtils.of(instance, NacosPluginInstance.class);
        nacosPluginInstance.setServiceName(namingEvent.getServiceName());
        nacosPluginInstance.setVersion(version);
        nacosPluginInstance.setGroup(namingEvent.getGroupName());

        List<NacosPluginInstance> cache = instanceCache.computeIfAbsent(namingEvent.getGroupName(), k -> new LinkedList<>());

        Optional<NacosPluginInstance> exist = cache
                .stream()
                .filter(c -> c.getServiceName().equals(nacosPluginInstance.getServiceName()))
                .findFirst();
        // 判断一下当前缓存是否存在同样的实例，如果存在，判断缓存的实例版本和当前的实例版本，如果当前实例版本较大，在覆盖一次資源内容
        if (exist.isPresent()) {

            NacosPluginInstance existData = exist.get();

            if (existData.getVersion().compareTo(nacosPluginInstance.getVersion()) > 0) {
                NacosSyncPluginResourceDto nacosSyncPluginResourceDto = new NacosSyncPluginResourceDto();
                nacosSyncPluginResourceDto.setInstance(nacosPluginInstance);
                return nacosSyncPluginResourceDto;
            }

            cache.remove(existData);
        }

        Map<String, Object> info = getInstanceInfo(instance);
        nacosPluginInstance.setPluginResources(info);

        cache.add(nacosPluginInstance);

        List<ResourceMetadata> data = enabledApplicationResource(nacosPluginInstance);

        NacosSyncPluginResourceDto nacosSyncPluginResourceDto = new NacosSyncPluginResourceDto();
        nacosSyncPluginResourceDto.setResources(data);
        nacosSyncPluginResourceDto.setInstance(nacosPluginInstance);
        return nacosSyncPluginResourceDto;
    }

    /**
     * 启用应用资源
     *
     * @param instance 插件实例
     */
    public List<ResourceMetadata> enabledApplicationResource(NacosPluginInstance instance) {

        if (Objects.isNull(instance) || Objects.isNull(instance.getVersion())) {
            return new LinkedList<>();
        }

        // 应用名称
        String applicationName = instance.getServiceName();

        if (log.isDebugEnabled()) {
            log.debug("开始绑定组为 [{}] 的 [{} {}] 应用资源信息", instance.getGroup(), applicationName, instance.getVersion());
        }

        List<PluginInfo> pluginList = createPluginInfoListFromInfo(instance.getPluginResources());
        // 启用資源得到新的資源集合
        List<ResourceMetadata> newResourceList = pluginList.stream()
                .map(p -> createResource(p, null, metadata -> this.appendInstanceInfo(metadata, instance)))
                .collect(Collectors.toList());

        return updateResourceMetadata(instance.getVersion(), newResourceList, applicationName);
    }

    private void appendInstanceInfo(
            ResourceMetadata target,
            NacosPluginInstance instance
    ) {
        if (StringUtils.isBlank(target.getApplicationName())) {
            target.setApplicationName(instance.getServiceName());
        }

        if (instance.getVersion() != null) {
            target.setVersion(instance.getVersion().toString());
        }
    }

    /**
     * 通过 info 信息创建插件信息实体集合
     *
     * @param info info 信息
     *
     * @return 插件信息实体集合
     */
    private List<PluginInfo> createPluginInfoListFromInfo(Map<String, Object> info) {

        List<PluginInfo> result = new LinkedList<>();

        List<Map<String, Object>> pluginMapList = CastUtils.cast(info.get(PluginEndpoint.DEFAULT_PLUGIN_KEY_NAME));
        if (CollectionUtils.isEmpty(pluginMapList)) {
            return result;
        }
        for (Map<String, Object> pluginMap : pluginMapList) {
            PluginInfo pluginInfo = createPluginInfo(pluginMap);
            result.add(pluginInfo);
        }

        return result;
    }

    /**
     * 通过插件 map 创建插件信息实体
     *
     * @param pluginMap 插件 map
     *
     * @return 插件信息实体
     */
    private PluginInfo createPluginInfo(Map<String, Object> pluginMap) {

        List<Map<String, Object>> children = new LinkedList<>();

        if (pluginMap.containsKey(PluginInfo.DEFAULT_CHILDREN_NAME)) {
            children = CastUtils.cast(pluginMap.get(PluginInfo.DEFAULT_CHILDREN_NAME));
            pluginMap.remove(PluginInfo.DEFAULT_CHILDREN_NAME);
        }

        PluginInfo pluginInfo = CastUtils.convertValue(pluginMap, PluginInfo.class);

        List<Tree<String, PluginInfo>> childrenNode = new LinkedList<>();

        pluginInfo.setChildren(childrenNode);

        for (Map<String, Object> child : children) {
            PluginInfo childNode = createPluginInfo(child);
            childrenNode.add(childNode);
        }

        return pluginInfo;
    }

    /**
     * 禁用资源
     *
     * @param event 服务变更时间
     */
    public DisabledApplicationResourceDto disabledApplicationResource(NamingEvent event) {

        // 从 Redis 获取资源
        RList<ResourceMetadata> redisResourceList = getRedisResourceList();
        List<ResourceMetadata> resources = new ArrayList<>(redisResourceList.readAll());

        // 从 Redis 中移除该应用的资源
        resources.removeIf(r -> r.getApplicationName().equals(event.getServiceName()));
        redisResourceList.clear();
        if (!resources.isEmpty()) {
            redisResourceList.addAll(resources);
        }

        // 清除组的实例缓存
        List<NacosPluginInstance> instances = instanceCache.computeIfAbsent(event.getGroupName(), k -> new LinkedList<>());
        instances.removeIf(p -> p.getServiceName().equals(event.getServiceName()));
        event.setInstances(new LinkedList<>(instances));
        return DisabledApplicationResourceDto.of(event, resources);
    }

}
