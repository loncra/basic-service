package io.github.loncra.basic.service.resource.server.controller.dictionary;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.resource.server.domain.entity.dictionary.DictionaryTypeEntity;
import io.github.loncra.basic.service.resource.server.service.dictionary.DictionaryService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.security.plugin.Plugin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 数据字典管理
 *
 * @author maurice
 * @see DictionaryTypeEntity
 */
@RestController
@RequestMapping("dictionary/type")
@Plugin(
        name = "字典类型管理",
        id = "dictionaryType",
        parent = "dictionary",
        authority = "resource_server_dictionary_type:find",
        type = SystemConstants.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class DictionaryTypeController {

    private final DictionaryService dictionaryService;

    /**
     * 获取所有字典类型
     *
     * @param request   http servlet request
     * @param mergeTree 是否合并树形，true 是，否则 false
     *
     * @return REST 响应结果
     */
    @PostMapping
    @PreAuthorize("hasAuthority('resource_server_dictionary_type:find')")
    public RestResult<List<DictionaryTypeEntity>> find(
            HttpServletRequest request,
            @RequestParam(required = false)
            boolean mergeTree
    ) {

        QueryWrapper<DictionaryTypeEntity> query = dictionaryService.getDictionaryTypeService()
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        List<DictionaryTypeEntity> dictionaryTypes = dictionaryService.getDictionaryTypeService()
                .find(query);

        if (mergeTree) {
            return RestResult.ofSuccess(TreeUtils.buildGenericTree(dictionaryTypes));
        }
        else {
            return RestResult.ofSuccess(dictionaryTypes);
        }
    }

    /**
     * 获取字典类型实体
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     */
    @GetMapping("/{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('resource_server_dictionary_type:get')")
    public DictionaryTypeEntity get(
            @PathVariable
            Long id
    ) {
        return dictionaryService.getDictionaryTypeService()
                .get(id);
    }

    /**
     * 保存数据字典类型
     *
     * @param entity 数据字典类型实体
     */
    @PutMapping
    @PreAuthorize("hasAuthority('resource_server_dictionary_type:save')")
    @Plugin(name = "添加或保存字典类型", operationDataTrace = true)
    public RestResult<Long> save(
            @Valid
            @RequestBody
            DictionaryTypeEntity entity
    ) {
        dictionaryService.saveDictionaryType(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除字典类型类型
     *
     * @param ids 主键值集合
     */
    @DeleteMapping
    @Plugin(name = "删除字典类型", operationDataTrace = true)
    @PreAuthorize("hasAuthority('resource_server_dictionary_type:delete')")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        dictionaryService.deleteDictionaryType(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断字典类型唯一识别值是否唯一
     *
     * @param code 唯一识别值
     *
     * @return REST 响应结果
     */
    @GetMapping("unique/code/{code}")
    @PreAuthorize("isAuthenticated()")
    public Boolean codeUnique(
            @PathVariable
            String code
    ) {

        return Objects.isNull(dictionaryService.getDictionaryTypeService().getByCode(code));
    }

}
