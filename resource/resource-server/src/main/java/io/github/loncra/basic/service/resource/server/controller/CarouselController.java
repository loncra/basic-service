package io.github.loncra.basic.service.resource.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.FlatSortMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.resource.server.domain.entity.CarouselEntity;
import io.github.loncra.basic.service.resource.server.service.CarouselService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 轮播图管理
 *
 * @author maurice.chen
 * @see CarouselEntity
 * @since 2025-05-25 08:27:31
 */
@RestController
@RequestMapping("carousel")
@Plugin(
        name = "轮播图管理",
        id = "carousel",
        parent = "config",
        authority = "perms[resource_server_carousel:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     *
     * @return REST 响应结果
     *
     * @see CarouselEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:page]')")
    public Page<CarouselEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<CarouselEntity> query = carouselService.getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByAsc(SystemConstants.SORT_FIELD);
        return carouselService.findTotalPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see CarouselEntity
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:get]')")
    @Plugin(name = "查看明细")
    public CarouselEntity get(
            @PathVariable
            Integer id
    ) {
        return carouselService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see CarouselEntity
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "保存或添加信息")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:save]')")
    public RestResult<Long> save(
            @Valid
            @RequestBody
            CarouselEntity entity
    ) {
        carouselService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 集合
     *
     * @see CarouselEntity
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:delete]')")
    public RestResult<Void> delete(
            @RequestParam
            List<Integer> ids
    ) {
        carouselService.deleteById(ids);
        return RestResult.of("删除 " + ids.size() + " 条记录成功");
    }

    /**
     * 发布数据
     *
     * @param ids 主键 ID 集合
     *
     * @return REST 响应结果
     */
    @OperationDataTrace
    @Plugin(name = "发布信息")
    @PostMapping("release")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:release]')")
    public RestResult<Void> release(
            @RequestParam
            List<Integer> ids
    ) {
        carouselService.publish(ids);
        return RestResult.of("发布 " + ids.size() + " 条记录成功");
    }

    /**
     * 下架数据
     *
     * @param ids 主键 ID 集合
     *
     * @return REST 响应结果
     */
    @OperationDataTrace
    @Plugin(name = "下架信息")
    @PostMapping("revoke")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:revoke]')")
    public RestResult<Void> deactivate(
            @RequestParam
            List<Integer> ids
    ) {
        carouselService.revoke(ids);
        return RestResult.of("下架 " + ids.size() + " 条记录成功");
    }

    @OperationDataTrace
    @Plugin(name = "排序")
    @PutMapping("sort")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:sort]')")
    public RestResult<Void> sort(
            @Valid
            @RequestBody
            List<FlatSortMetadata<Long>> sorts
    ) {
        carouselService.sort(sorts);
        return RestResult.of("排序成功");
    }
}
