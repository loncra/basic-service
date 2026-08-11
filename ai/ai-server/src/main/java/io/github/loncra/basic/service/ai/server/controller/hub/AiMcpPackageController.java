package io.github.loncra.basic.service.ai.server.controller.hub;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
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
 *
 * tb_ai_mcp_package 的控制器
 *
 * @see AiMcpPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@RestController
@RequestMapping("ai/mcp/package")
@Plugin(
    name = "MCP 连接器目录",
    id = "ai_mcp_package",
    parent = "system",
    authority = "perms[ai_mcp_package:page]",
    type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class AiMcpPackageController {

    private final AiMcpPackageService aiMcpPackageService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see AiMcpPackageEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[ai_mcp_package:page]')")
    public Page<AiMcpPackageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<AiMcpPackageEntity> query = aiMcpPackageService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return aiMcpPackageService.findPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see AiMcpPackageEntity
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[ai_mcp_package:get]')")
    @Plugin(name = "查看明细")
    public AiMcpPackageEntity get(@RequestParam Integer id) {
        return aiMcpPackageService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see AiMcpPackageEntity
     */
    @PutMapping
    @PreAuthorize("hasAuthority('perms[ai_mcp_package:save]')")
    @Plugin(name = "保存或添加信息")
    public RestResult<Long> save(@Valid @RequestBody AiMcpPackageEntity entity) {
        aiMcpPackageService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see AiMcpPackageEntity
     */
    @DeleteMapping
    @PreAuthorize("hasAuthority('perms[ai_mcp_package:delete]')")
    @Plugin(name = "删除信息")
    public RestResult<Void> delete(@RequestParam List<Integer> ids) {
        aiMcpPackageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
