package io.github.loncra.basic.service.auth.server.service.resource.plugin.scan;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import io.github.loncra.basic.service.auth.server.domain.dto.ScanSyncPluginResourceDto;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.AbstractPluginResourceService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.PluginInfo;
import io.github.loncra.framework.spring.security.core.authentication.config.PluginProperties;
import io.github.loncra.framework.spring.security.core.plugin.PluginEndpoint;
import io.github.loncra.framework.spring.web.endpoint.EnumerateEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 扫描插件模块的资源服务实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScanModulePluginResourceService extends AbstractPluginResourceService implements InitializingBean {

    private final PluginProperties pluginProperties;

    /**
     * 信息奉献者集合
     */
    private final List<InfoContributor> infoContributors;

    //private final AmqpTemplate amqpTemplate;
    private final ApplicationEventPublisher publisher;

    @Override
    public void resubscribeAllService() {
        throw new UnsupportedOperationException("单体服务，不支持重新订阅所有服务功能");
    }

    @Override
    public void afterPropertiesSet() {

        Map<String, Object> info = EnumerateEndpoint.getInfoContributorsMap(this.infoContributors);
        String version = info.getOrDefault(PluginInfo.DEFAULT_VERSION_NAME, StringUtils.EMPTY).toString();
        String groupId = info.getOrDefault(PluginInfo.DEFAULT_GROUP_ID_NAME, StringUtils.EMPTY).toString();
        String artifactId = info.getOrDefault(PluginInfo.DEFAULT_ARTIFACT_ID_NAME, StringUtils.EMPTY).toString();

        for (Map.Entry<String, List<String>> entry : getAuthAppConfig().getScanPluginPackages().entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug("开始绑定 [{}] 应用插件资源信息", entry.getKey());
            }

            PluginProperties module = CastUtils.of(pluginProperties, PluginProperties.class);
            module.setBasePackages(entry.getValue());

            PluginEndpoint pluginEndpoint = new PluginEndpoint(module);
            pluginEndpoint.init();

            Set<Object> objects = pluginEndpoint.resolvePlaceholders();
            List<PluginInfo> modulePlugins = pluginEndpoint.getPluginInfos(objects);
            modulePlugins.addAll(module.getParent().values());
            modulePlugins = TreeUtils.buildGenericTree(modulePlugins);
            List<ResourceMetadata> newResourceList = modulePlugins.stream()
                    .map(p -> createResource(p, metadata -> this.appendModuleInfo(entry.getKey(), metadata)))
                    .toList();
            Version versionObject = VersionUtil.parseVersion(version, groupId, artifactId);
            updateResourceMetadata(versionObject, newResourceList, entry.getKey());
            ScanSyncPluginResourceDto dto = new ScanSyncPluginResourceDto();
            dto.setServiceName(entry.getKey());
            dto.setResources(newResourceList);
            publisher.publishEvent(new AbstractPluginResourceService.SyncPluginResourceEvent(dto));
        }
    }

    private void appendModuleInfo(String applicationName, ResourceMetadata metadata) {
        Map<String, Object> info = EnumerateEndpoint.getInfoContributorsMap(this.infoContributors);
        String version = info.getOrDefault(PluginInfo.DEFAULT_VERSION_NAME, StringUtils.EMPTY).toString();
        metadata.setVersion(version);
        metadata.setApplicationName(applicationName);
    }
}
