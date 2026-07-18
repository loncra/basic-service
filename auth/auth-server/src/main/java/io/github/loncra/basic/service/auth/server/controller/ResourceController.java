package io.github.loncra.basic.service.auth.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.service.resource.ResourceService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.TreeSortMetadata;
import io.github.loncra.basic.service.commons.enumerate.DefaultOperateCategoryEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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

    private final ResourceService resourceService;

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
            @RequestParam(required = false, defaultValue = "true")
            boolean mergeTree,
            HttpServletRequest request
    ) {
        QueryWrapper<ResourceEntity> query = resourceService.getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByAsc(SystemConstants.SORT_FIELD);
        List<ResourceEntity> resourceList = resourceService.find(query);
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
        return resourceService.get(id);
    }

    @PutMapping
    @OperationDataTrace
    @Plugin(name = "添加或保存信息")
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:save]')")
    public RestResult<Long> save(
            @Valid
            @RequestBody
            ResourceEntity entity
    ) {
        if (Objects.isNull(entity.getId())) {
            entity.setCategory(DefaultOperateCategoryEnum.CUSTOMIZE);
        }
        resourceService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    @DeleteMapping
    @OperationDataTrace
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:delete]')")
    @Plugin(name = "删除信息")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        resourceService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    @OperationDataTrace
    @PutMapping("sort")
    @Plugin(name = "排序")
    @PreAuthorize("hasAuthority('perms[auth_server_authority_resource:sort]')")
    public RestResult<Void> sort(
            @Valid
            @RequestBody
            List<TreeSortMetadata<Long>> sorts
    ) {
        resourceService.sort(sorts);
        return RestResult.of("排序成功");
    }

}
