package io.github.loncra.basic.service.resource.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.resource.server.domain.entity.CarouselEntity;
import io.github.loncra.basic.service.resource.server.service.CarouselService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
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
    @PostMapping
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:page]')")
    public Page<CarouselEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<CarouselEntity> query = carouselService.getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
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
            @RequestParam
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
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:save]')")
    @Plugin(name = "保存或添加信息", operationDataTrace = true)
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
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:delete]')")
    @Plugin(name = "删除信息", operationDataTrace = true)
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
    @PostMapping("publish")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:publish]')")
    @Plugin(name = "发布信息", operationDataTrace = true)
    public RestResult<Void> publish(
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
    @PostMapping("deactivate")
    @PreAuthorize("hasAuthority('perms[resource_server_carousel:deactivate]')")
    @Plugin(name = "下架信息", operationDataTrace = true)
    public RestResult<Void> deactivate(
            @RequestParam
            List<Integer> ids
    ) {
        carouselService.deactivate(ids);
        return RestResult.of("下架 " + ids.size() + " 条记录成功");
    }

}
