package io.github.loncra.basic.service.auth.server.service.plugin;

import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;

import java.util.List;
import java.util.Set;

public interface PluginResourceService {

    List<ResourceMetadata> getResourcesStream(
            List<IdResourceAuthorityMetadata> resources,
            ResourceSourceEnum... sources
    );

    Set<String> getPluginServerNames();

    List<ResourceMetadata> getResources();

    List<ResourceMetadata> getResources(
            String applicationName,
            ResourceSourceEnum... sources
    );

    void resubscribeAllService() throws Exception;
}
