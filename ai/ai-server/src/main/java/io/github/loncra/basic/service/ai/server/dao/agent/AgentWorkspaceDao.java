package io.github.loncra.basic.service.ai.server.dao.agent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_agent_workspace 的数据访问
 *
 * <p>Table: tb_agent_workspace - agent 工作空间</p>
 *
 * @see AgentWorkspaceEntity
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Mapper
@Repository
public interface AgentWorkspaceDao extends BaseMapper<AgentWorkspaceEntity> {

}
