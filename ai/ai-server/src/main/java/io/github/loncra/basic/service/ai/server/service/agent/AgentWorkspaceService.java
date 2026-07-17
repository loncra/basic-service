package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.dao.agent.AgentWorkspaceDao;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * tb_agent_workspace 的业务逻辑
 *
 * <p>Table: tb_agent_workspace - agent 工作空间</p>
 *
 * @see AgentWorkspaceEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Service
@RequiredArgsConstructor
public class AgentWorkspaceService extends BasicService<AgentWorkspaceDao, AgentWorkspaceEntity> {

}
