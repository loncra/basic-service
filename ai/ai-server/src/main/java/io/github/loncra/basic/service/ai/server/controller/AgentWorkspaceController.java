package io.github.loncra.basic.service.ai.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import io.github.loncra.basic.service.ai.server.service.agent.AgentWorkspaceService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_agent_workspace 的控制器
 *
 * @see AgentWorkspaceEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@RestController
@RequestMapping("agent/workspace")
@RequiredArgsConstructor
public class AgentWorkspaceController {

    private final AgentWorkspaceService agentWorkspaceService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see AgentWorkspaceEntity
     */
    @PostMapping
    public Page<AgentWorkspaceEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<AgentWorkspaceEntity> query = agentWorkspaceService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return agentWorkspaceService.findPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see AgentWorkspaceEntity
     */
    @GetMapping("/{id:\\d+}")
    public AgentWorkspaceEntity get(@PathVariable Integer id) {
        return agentWorkspaceService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see AgentWorkspaceEntity
     */
    @PutMapping
    public RestResult<Long> save(@Valid @RequestBody AgentWorkspaceEntity entity) {
        agentWorkspaceService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see AgentWorkspaceEntity
     */
    @DeleteMapping
    public RestResult<Void> delete(@RequestParam List<Integer> ids) {
        agentWorkspaceService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
