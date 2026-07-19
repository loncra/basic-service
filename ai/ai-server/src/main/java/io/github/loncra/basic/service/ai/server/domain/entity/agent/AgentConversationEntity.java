package io.github.loncra.basic.service.ai.server.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_agent_conversation - agent 对话</p>
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Data
@NoArgsConstructor
@Alias("agentConversation")
@TableName("tb_agent_conversation")
@EqualsAndHashCode(callSuper = true)
public class AgentConversationEntity extends LongVersionEntity<Integer> implements TenantEntity<String>, AuditPrincipal {

    @Serial
    private static final long serialVersionUID = -4801789299046545458L;

    /**
     * 名称
     */
    private String name;

    /**
     * 工作空间 id
     */
    private Long agentWorkspaceId;

    /**
     * 状态
     */
    private AgentChatStatusEnum status;

    private String principal;

    private String tenantId;

}