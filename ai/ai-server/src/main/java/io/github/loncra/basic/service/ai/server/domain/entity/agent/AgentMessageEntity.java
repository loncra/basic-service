package io.github.loncra.basic.service.ai.server.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


/**
 * <p>Table: tb_agent_message - agent 对话消息</p>
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Data
@NoArgsConstructor
@Alias("agentMessage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_agent_message", autoResultMap = true)
public class AgentMessageEntity extends AgentChatMetadata implements VersionEntity<Integer, Long>, AuditPrincipal, TenantEntity<String> {

    @Serial
    private static final long serialVersionUID = -5121909839700678113L;

    public static final String CONVERSATION_ID_TABLE_FIELD_NAME = "agent_conversation_id";

    private Long id;

    @Version
    private Integer version;

    private Instant creationTime;

    /**
     * 角色
     */
    private AgentMessageRoleEnum role;

    /**
     * 对话 id
     */
    private Long agentConversationId;

    private AgentChatStatusEnum status;

    private String principal;

    private String tenantId;
}