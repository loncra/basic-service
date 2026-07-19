package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.dao.agent.AgentConversationDao;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * tb_agent_conversation 的业务逻辑
 *
 * <p>Table: tb_agent_conversation - agent 对话</p>
 *
 * @see AgentConversationEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Service
@RequiredArgsConstructor
public class AgentConversationService extends BasicService<AgentConversationDao, AgentConversationEntity> {

    public List<AgentConversationEntity> findByAgentWorkspaceId(Long agentWorkspaceId) {
        return lambdaQuery().eq(AgentConversationEntity::getAgentWorkspaceId, agentWorkspaceId).list();
    }
}
