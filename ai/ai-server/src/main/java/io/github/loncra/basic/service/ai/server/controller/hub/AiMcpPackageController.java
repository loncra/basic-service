package io.github.loncra.basic.service.ai.server.controller.hub;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.api.constants.AiConstants;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 *
 * tb_ai_mcp_package 的控制器
 *
 * @author maurice.chen
 * @see AiMcpPackageEntity
 * @since 2026-08-04 09:21:08
 */
@RestController
@RequestMapping("ai/mcp/package")
@Plugin(
        name = "MCP 广场配置",
        id = "ai_mcp_package",
        parent = "config",
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
     * @param request     http servlet request
     * @return 分页实体
     * @see AiMcpPackageEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[ai_mcp_package:page]')")
    public Page<AiMcpPackageEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
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
     * @return REST 响应结果
     * @see AiMcpPackageEntity
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[ai_mcp_package:get]')")
    @Plugin(name = "查看明细")
    public AiMcpPackageEntity get(@PathVariable Integer id) {
        return aiMcpPackageService.get(id);
    }

    /**
     * 按当前传输配置临时建连并拉取 MCP 可调用工具（不写入缓存）
     *
     * @param name mcp 形成
     * @param client 客户端配置信息
     *
     * @return 工具简要列表
     */
    @PostMapping("tools/{name}")
    public List<McpSchema.Tool> tools(
            @PathVariable(required = false) String name,
            @RequestBody Map<String, Object> client
    ) {
        name = StringUtils.defaultIfEmpty(name, AiConstants.newReplyId());
        return aiMcpPackageService.remoteTools(name, client);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
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
