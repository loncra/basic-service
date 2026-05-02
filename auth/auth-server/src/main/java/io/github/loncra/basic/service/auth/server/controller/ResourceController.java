package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import io.github.loncra.basic.service.auth.server.service.plugin.DelegatingPluginResourceService;
import io.github.loncra.basic.service.auth.server.service.role.RoleService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
        authority = "auth_server_authority_resource:find",
        type = SystemConstants.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class ResourceController {

    private final RoleService roleService;

    private final DelegatingPluginResourceService delegatingPluginResourceService;

    /**
     * 查找资源
     *
     * @param mergeTree 合并树行
     *
     * @return 资源实体集合
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public List<ResourceMetadata> find(
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

        List<ResourceMetadata> resourceList = roleService
                .getPluginResourceService()
                .getResources(applicationName, resourceSources.toArray(new ResourceSourceEnum[0]));

        if (mergeTree) {
            return TreeUtils.buildGenericTree(resourceList);
        }
        else {
            return resourceList;
        }
    }

    /**
     * 获取当前用户资源
     *
     * @param securityContext 安全上下文
     * @param mergeTree       是否合并树形 true，是 否则 false
     *
     * @return 资源实体集合
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("consolePrincipalResources")
    public List<ResourceMetadata> getConsolePrincipalResources(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(required = false)
            List<String> types,
            @RequestParam(required = false)
            boolean mergeTree
    ) {

        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());

        List<ResourceSourceEnum> sourceContains = Collections.singletonList(
                NameEnum.ofEnum(ResourceSourceEnum.class, token.getType())
        );

        List<ResourceMetadata> resourceList = roleService.getSystemUserResource(
                token,
                types,
                sourceContains
        );

        List<ResourceMetadata> result = resourceList
                .stream()
                .sorted(Comparator.comparing(ResourceMetadata::getSort).reversed())
                .toList();

        if (mergeTree) {
            return TreeUtils.buildGenericTree(result);
        }
        else {
            return result;
        }
    }

    /**
     * 获取资源
     *
     * @param id 主键值
     *
     * @return 资源实体
     */
    @GetMapping("/{id}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('auth_server_authority_resource:get')")
    public ResourceMetadata get(
            @PathVariable
            String id
    ) {

        return roleService
                .getPluginResourceService()
                .getResources(null)
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 同步插件資源
     *
     * @return reset 结果集
     */
    @PostMapping("plugin/sync")
    @Plugin(name = "同步插件资源", audit = true)
    @PreAuthorize("hasAuthority('auth_server_authority_resource:sync_plugin_resource')")
    public RestResult<Void> syncPluginResource() throws Exception {
        delegatingPluginResourceService.resubscribeAllService();
        return RestResult.of("同步数据完成");
    }
}
