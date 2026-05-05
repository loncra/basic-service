package io.github.loncra.basic.service.auth.server.service.resource.plugin;

import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;

import java.util.List;
import java.util.Set;

public interface PluginResourceService {

    List<ResourceEntity> getResourcesStream(
            List<Long> resourceIds,
            ResourceSourceEnum... sources
    );

    Set<String> getPluginServerNames();

    List<ResourceEntity> getResources();

    List<ResourceEntity> getResources(
            String applicationName,
            ResourceSourceEnum... sources
    );

    void resubscribeAllService() throws Exception;
}
