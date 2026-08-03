package io.github.loncra.basic.service.ai.server.controller.agent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.service.agent.AgentConversationService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.tree.TreeUtils;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
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
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see AgentConversationEntity
     */
    @PostMapping
    public List<AgentConversationEntity> find(
            HttpServletRequest request,
            @CurrentSecurityContext SecurityContext context,
            @RequestParam(required = false, defaultValue = "true")
            boolean mergeTree
    ) {
        QueryWrapper<AgentConversationEntity> query = agentConversationService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);

        query.eq(SystemConstants.PRINCIPAL_FIELD_NAME, context.getAuthentication().getName());
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        List<AgentConversationEntity> result = agentConversationService.find(query);
        if (mergeTree) {
            return TreeUtils.buildGenericTree(result);
        }
        return result;
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see AgentMessageEntity
     */
    /*@DeleteMapping
    public RestResult<Void> delete(@RequestParam List<Integer> ids) {
        agentConversationService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }*/

    @PutMapping
    @OperationDataTrace("创建工作空间")
    public RestResult<Long> save(
            @Valid
            @RequestBody
            AgentConversationEntity entity
    ) {
        agentConversationService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

}
