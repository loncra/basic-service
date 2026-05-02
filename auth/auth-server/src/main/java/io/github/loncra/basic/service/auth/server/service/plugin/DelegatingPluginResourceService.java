package io.github.loncra.basic.service.auth.server.service.plugin;

import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.service.plugin.disconvery.NacosDiscoveryPluginResourceService;
import io.github.loncra.basic.service.auth.server.service.plugin.scan.ScanModulePluginResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;
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
    public List<ResourceMetadata> getResourcesStream(
            List<IdResourceAuthorityMetadata> resources,
            ResourceSourceEnum... sources
    ) {
        return getDelegate().getResourcesStream(resources, sources);
    }

    @Override
    public Set<String> getPluginServerNames() {
        return getDelegate().getPluginServerNames();
    }

    @Override
    public List<ResourceMetadata> getResources() {
        return getDelegate().getResources();
    }

    @Override
    public List<ResourceMetadata> getResources(
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
