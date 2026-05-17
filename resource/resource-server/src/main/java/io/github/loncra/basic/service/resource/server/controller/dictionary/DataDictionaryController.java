package io.github.loncra.basic.service.resource.server.controller.dictionary;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.TreeSortMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DataDictionaryEntity;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DictionaryTypeEntity;
import io.github.loncra.basic.service.resource.server.service.dictionary.DictionaryService;
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
import java.util.Map;
import java.util.Objects;

/**
 * 数据字典管理
 *
 * @author maurice
 * @see DataDictionaryEntity
 */
@RestController
@RequestMapping("data/dictionary")
@Plugin(
        name = "数据字典管理",
        id = "data_dictionary",
        parent = "dictionary",
        authority = "perms[resource_server_data_dictionary:page]",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class DataDictionaryController {

    private final DictionaryService dictionaryService;

    /**
     * 获取数据字典分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     *
     * @return REST 响应结果
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[resource_server_data_dictionary:page]')")
    public Page<DataDictionaryEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<DataDictionaryEntity> query = dictionaryService.getDataDictionaryService()
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByAsc(SystemConstants.SORT_FIELD);

        return dictionaryService.getDataDictionaryService()
                .findTotalPage(pageRequest, query);
    }

    /**
     * 获取数据字典
     *
     * @param id 数据字典 ID
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[resource_server_data_dictionary:get]')")
    @Plugin(name = "查看明细")
    public DataDictionaryEntity get(
            @PathVariable
            Long id
    ) {
        return dictionaryService.getDataDictionaryService()
                .get(id);
    }

    /**
     * 保存数据字典
     *
     * @param entity 数据字典实体
     */
    @PutMapping
    @PreAuthorize("hasAuthority('perms[resource_server_data_dictionary:save]')")
    @Plugin(name = "添加或保存数据字典", operationDataTrace = true)
    public RestResult<Long> save(
            @Valid
            @RequestBody
            DataDictionaryEntity entity
    ) {
        dictionaryService.saveDataDictionary(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据字典
     *
     * @param ids 主键值集合
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('perms[resource_server_data_dictionary:delete]')")
    @Plugin(name = "删除数据字典实体", operationDataTrace = true)
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        dictionaryService.deleteDataDictionary(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断数据字典唯一识别值是否唯一
     *
     * @param code 唯一识别值
     *
     * @return REST 响应结果
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("unique/code/{code}")
    public Boolean codeUnique(
            @PathVariable
            String code
    ) {
        return Objects.isNull(dictionaryService.getDataDictionaryService().getByCode(code));
    }

    /**
     * 根据字典类型查询数据字典数据并分组
     *
     * @param typeIds 字典类型 id 集合
     *
     * @return REST 响应结果
     */
    @PostMapping("group")
    public Map<Long, List<DataDictionaryMetadata>> groupByTypeIds(
            @RequestParam
            List<Long> typeIds
    ) {
        return dictionaryService.findGroupDataDictionariesByTypeIds(typeIds);
    }

    @PutMapping("sort")
    @PreAuthorize("hasAuthority('perms[resource_server_data_dictionary:sort]')")
    @Plugin(name = "排序", operationDataTrace = true)
    public RestResult<Void> sort(
            @Valid
            @RequestBody
            List<TreeSortMetadata<Long>> sorts
    ) {
        dictionaryService.getDataDictionaryService().sort(sorts);
        return RestResult.of("排序成功");
    }
}
