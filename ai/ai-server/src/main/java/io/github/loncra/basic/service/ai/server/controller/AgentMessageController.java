package io.github.loncra.basic.service.ai.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;


/**
 *
 * tb_agent_message 的控制器
 *
 * @see AgentMessageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@RestController
@RequestMapping("agent/message")
@RequiredArgsConstructor
public class AgentMessageController {

    private final AgentMessageService agentMessageService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see AgentMessageEntity
     */
    @PostMapping
    public Page<AgentMessageEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<AgentMessageEntity> query = agentMessageService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return agentMessageService.findPage(pageRequest, query);
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see AgentMessageEntity
     */
    @DeleteMapping
    public RestResult<Void> delete(@RequestParam List<Integer> ids) {
        agentMessageService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
