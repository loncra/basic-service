package io.github.loncra.basic.service.auth.server.service.resource.plugin;

import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.disconvery.NacosDiscoveryPluginResourceService;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.scan.ScanModulePluginResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Primary
@Component
@RequiredArgsConstructor
public class DelegatingPluginResourceService implements PluginResourceService {

    private final ObjectProvider<NacosDiscoveryPluginResourceService> nacosDiscoveryPluginResourceServiceProvider;

    private final ScanModulePluginResourceService scanModulePluginResourceService;

    private PluginResourceService getDelegate() {
        PluginResourceService nacosService = nacosDiscoveryPluginResourceServiceProvider.getIfAvailable();
        return nacosService != null ? nacosService : scanModulePluginResourceService;
    }

    @Override
    public List<ResourceEntity> getResourcesStream(
            List<Long> resourceIds,
            ResourceSourceEnum... sources
    ) {
        return getDelegate().getResourcesStream(resourceIds, sources);
    }

    @Override
    public Set<String> getPluginServerNames() {
        return getDelegate().getPluginServerNames();
    }

    @Override
    public List<ResourceEntity> getResources() {
        return getDelegate().getResources();
    }

    @Override
    public List<ResourceEntity> getResources(
            String applicationName,
            ResourceSourceEnum... sources
    ) {
        return getDelegate().getResources(applicationName, sources);
    }

    @Override
    public void resubscribeAllService() throws Exception {
        getDelegate().resubscribeAllService();
    }
}
