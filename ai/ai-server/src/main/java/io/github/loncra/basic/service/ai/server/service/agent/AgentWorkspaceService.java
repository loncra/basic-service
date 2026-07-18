package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.dao.agent.AgentWorkspaceDao;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentWorkspaceEntity;
import io.github.loncra.basic.service.commons.enumerate.DefaultOperateCategoryEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

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

    private final AiAppConfig aiAppConfig;

    public AgentWorkspaceEntity getDefaultWorkspace(String getPrincipal) {
        return lambdaQuery().eq(AgentWorkspaceEntity::getPrincipal, getPrincipal)
                .eq(AgentWorkspaceEntity::getName, aiAppConfig.getDefaultWorkspaceName())
                .one();
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentWorkspaceEntity createDefaultIfNotExist(AuditAuthenticationToken token) {

        AgentWorkspaceEntity entity = getDefaultWorkspace(token.getName());

        if (Objects.isNull(entity)) {
            entity = new AgentWorkspaceEntity();
            entity.setName(aiAppConfig.getDefaultWorkspaceName());
            entity.setPrincipal(token.getName());
            entity.setOperateCategory(DefaultOperateCategoryEnum.SYSTEM);
            insert(entity);
        }

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Collection<? extends Serializable> ids,
            boolean errorThrow,
            boolean useFill
    ) {
        int result = ids.stream().mapToInt(this::deleteById).sum();
        if (result != ids.size() && errorThrow) {
            String msg = "删除智能体工作空间 ID 为 [" + ids + "] 的数据发生异常";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Serializable id,
            boolean useFill
    ) {
        return deleteByEntity(get(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(AgentWorkspaceEntity entity) {
        SystemException.isTrue(!DefaultOperateCategoryEnum.SYSTEM.equals(entity.getOperateCategory()), "无法删除 [" + entity.getOperateCategory() + "]类型的空间");
        return super.deleteByEntity(entity);
    }
}
