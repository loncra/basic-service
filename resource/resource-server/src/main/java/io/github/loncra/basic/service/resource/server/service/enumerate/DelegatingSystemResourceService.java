package io.github.loncra.basic.service.resource.server.service.enumerate;

import io.github.loncra.basic.service.resource.server.service.enumerate.disconvery.NacosDiscoverySystemResourceService;
import io.github.loncra.basic.service.resource.server.service.enumerate.scan.ScanModuleSystemResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Primary
@Component
@RequiredArgsConstructor
public class DelegatingSystemResourceService implements SystemResourceService {

    private final ObjectProvider<NacosDiscoverySystemResourceService> nacosDiscoverySystemResourceServiceProvider;

    private final ScanModuleSystemResourceService scanModuleSystemResourceService;

    private SystemResourceService getDelegate() {
        SystemResourceService nacosService = nacosDiscoverySystemResourceServiceProvider.getIfAvailable();
        return nacosService != null ? nacosService : scanModuleSystemResourceService;
    }

    @Override
    public Map<String, Map<String, Map<String, Object>>> getServiceEnumerate() {
        return getDelegate().getServiceEnumerate();
    }

    @Override
    public void syncEnumerate() throws Exception {
        getDelegate().syncEnumerate();
    }

    @Override
    public Map<String, Object> getServiceEnumerate(
            String key,
            String id,
            List<String> value
    ) {
        return getDelegate().getServiceEnumerate(key, id, value);
    }
}
