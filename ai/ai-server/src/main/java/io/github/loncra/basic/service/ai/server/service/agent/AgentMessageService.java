package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.dao.agent.AgentMessageDao;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * tb_agent_message 的业务逻辑
 *
 * <p>Table: tb_agent_message - agent 对话消息</p>
 *
 * @see AgentMessageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Service
@RequiredArgsConstructor
public class AgentMessageService extends BasicService<AgentMessageDao, AgentMessageEntity> {

    public int positioningPageNumber(
            Long agentConversationId,
            Long messageId,
            int pageSize
    ) {
        long newerCount = lambdaQuery().eq(AgentMessageEntity::getAgentConversationId, agentConversationId)
                .gt(AgentMessageEntity::getId, messageId)
                .count();
        return (int) (newerCount / pageSize) + 1;
    }

    public List<AgentMessageEntity> findByConversationId(Long agentConversationId) {
        return lambdaQuery().eq(AgentMessageEntity::getAgentConversationId, agentConversationId).list();
    }
}
