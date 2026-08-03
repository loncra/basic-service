package io.github.loncra.basic.service.ai.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.service.ModelSettingService;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.TreeSortMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.IdEntity;
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
 *
 * tb_model_setting 的控制器
 *
 * @see ModelSettingEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-30 07:51:00
 */
@RestController
@RequestMapping("model/setting")
@Plugin(
    name = "模型配置",
    id = "mode_setting",
    parent = "config",
    authority = "perms[ai_server_mode_setting:find]",
    type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class ModelSettingController {

    private final ModelSettingService modelSettingService;

    @PostMapping("enabled")
    public List<ModelSettingEntity> findEnabled(HttpServletRequest request){
        QueryWrapper<ModelSettingEntity> query = modelSettingService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByAsc(SystemConstants.SORT_FIELD);
        query.eq(SystemConstants.ENABLED_FIELD_NAME, YesOrNo.Yes.getValue());
        return modelSettingService.find(query);
    }

    /**
     * 获取分页
     *
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see ModelSettingEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[ai_server_mode_setting:find]')")
    public List<ModelSettingEntity> find(HttpServletRequest request) {
        QueryWrapper<ModelSettingEntity> query = modelSettingService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByAsc(SystemConstants.SORT_FIELD);
        return modelSettingService.find(query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see ModelSettingEntity
     */
    @GetMapping("/{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[ai_server_mode_setting:get]')")
    public ModelSettingEntity get(@PathVariable Integer id) {
        return modelSettingService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see ModelSettingEntity
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "保存或添加信息")
    @PreAuthorize("hasAuthority('perms[ai_server_mode_setting:save]')")
    public RestResult<Long> save(@Valid @RequestBody ModelSettingEntity entity) {
        modelSettingService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see ModelSettingEntity
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[ai_server_mode_setting:delete]')")
    public RestResult<Void> delete(@RequestParam List<Long> ids) {
        modelSettingService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    @OperationDataTrace
    @PutMapping("sort")
    @Plugin(name = "排序")
    @PreAuthorize("hasAuthority('perms[ai_server_mode_setting:sort]')")
    public RestResult<Void> sort(
            @Valid
            @RequestBody
            List<TreeSortMetadata<Long>> sorts
    ) {
        modelSettingService.sort(sorts);
        return RestResult.of("排序成功");
    }
}
