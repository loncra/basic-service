package io.github.loncra.basic.service.ai.server.service.agent;

import io.agentscope.core.state.AgentStateStore;
import io.github.loncra.basic.service.ai.server.config.ConversationConfig;
import io.github.loncra.basic.service.ai.server.dao.agent.AgentConversationDao;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

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

    private final ConversationConfig conversationConfig;

    private final AgentStateStore agentStateStore;

    public AgentConversationEntity getDefaultWorkspace(String getPrincipal) {
        return lambdaQuery().eq(AgentConversationEntity::getPrincipal, getPrincipal)
                .eq(AgentConversationEntity::getType, AgentConversationTypeEnum.DEFAULT_WORKSPACE.getValue())
                .one();
    }

    @Transactional(rollbackFor = Exception.class)
    public AgentConversationEntity createDefaultIfNotExist(AuditAuthenticationToken token) {

        AgentConversationEntity entity = getDefaultWorkspace(token.getName());

        if (Objects.isNull(entity)) {
            entity = new AgentConversationEntity();
            entity.setName(conversationConfig.getDefaultWorkspaceName());
            entity.setPrincipal(token.getName());
            entity.setStatus(AgentChatStatusEnum.READY);
            entity.setGenerateName(YesOrNo.Yes);
            entity.setType(AgentConversationTypeEnum.DEFAULT_WORKSPACE);
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

   /* @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(AgentConversationEntity entity) {
        SystemException.isTrue(!AgentConversationTypeEnum.DEFAULT_WORKSPACE.equals(entity.getType()), "无法删除 [" + entity.getType().getName() + "]类型的空间");
        lambdaQuery().eq(AgentConversationEntity::getParentId, entity.getId()).list().forEach(this::deleteByEntity);
        agentStateStore.delete(Strings.CS.replace(entity.getPrincipal(), CacheProperties.DEFAULT_SEPARATOR, CastUtils.UNDERSCORE), entity.getId().toString());
        return super.deleteByEntity(entity);
    }*/

    @Override
    public int insert(AgentConversationEntity entity) {
        entity.setGenerateName(YesOrNo.ofBoolean(!conversationConfig.isEnabled()));
        entity.setName(StringUtils.defaultIfEmpty(entity.getName(), conversationConfig.getNewConversation()));
        entity.setStatus(AgentChatStatusEnum.READY);
        return super.insert(entity);
    }
}
