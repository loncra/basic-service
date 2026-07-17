package io.github.loncra.basic.service.ai.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
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
 * tb_agent_conversation 的控制器
 *
 * @see AgentConversationEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@RestController
@RequestMapping("agent/conversation")
@RequiredArgsConstructor
public class AgentConversationController {

    private final AgentConversationService agentConversationService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see AgentConversationEntity
     */
    @PostMapping
    public Page<AgentConversationEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<AgentConversationEntity> query = agentConversationService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return agentConversationService.findPage(pageRequest, query);
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
        agentConversationService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
