package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.service.resource.plugin.DelegatingPluginResourceService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 资源管理
 *
 * @author maurice.chen
 **/
@RestController
@RequestMapping("resource")
@Plugin(
        name = "资源管理",
        id = "authority_resource",
        parent = "authority",
        authority = "perms[auth_server_authority_resource:find]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class ResourceController {

    private final DelegatingPluginResourceService delegatingPluginResourceService;

    /**
     * 查找资源
     *
     * @param mergeTree 合并树行
     *
     * @return 资源实体集合
     */
    @PostMapping("find")
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:find]')")
    public List<ResourceEntity> find(
            @RequestParam(required = false)
            boolean mergeTree,
            @RequestParam(required = false)
            String applicationName,
            @RequestParam(required = false)
            List<String> sources
    ) {

        List<ResourceSourceEnum> resourceSources = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(sources)) {
            resourceSources = sources
                    .stream()
                    .map(s -> NameEnum.ofEnum(ResourceSourceEnum.class, s))
                    .toList();
        }

        List<ResourceEntity> resourceList = delegatingPluginResourceService
                .getResources(applicationName, resourceSources.toArray(new ResourceSourceEnum[0]));

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        }
        else {
            return resourceList;
        }
    }

    /**
     * 获取资源
     *
     * @param id 主键值
     *
     * @return 资源实体
     */
    @GetMapping("{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:get]')")
    public ResourceEntity get(
            @PathVariable
            Long id
    ) {
        List<ResourceEntity> list = delegatingPluginResourceService
                .getResourcesStream(Set.of(id));
        return CollectionUtils.isNotEmpty(list) ? list.getFirst() : null;
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @PostMapping("plugin/sync")
    @Plugin(name = "同步插件资源", audit = true)
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:sync_plugin_resource]')")
    public RestResult<Void> syncPluginResource() throws Exception {
        delegatingPluginResourceService.resubscribeAllService();
        return RestResult.of("同步数据完成");
    }
}
