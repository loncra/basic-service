package io.github.loncra.basic.service.ai.server.controller.agent;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import io.github.loncra.basic.service.ai.server.service.agent.AgentWorkspaceService;
import io.github.loncra.framework.commons.RestResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
