package io.github.loncra.basic.service.ai.server.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;


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
public class AgentMessageEntity extends AgentChatMetadata implements VersionEntity<Integer, Long>, AuditPrincipal, TenantEntity<String>, Tree<Long, AgentMessageEntity> {

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

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private ModelSettingMetadata model;

    private String principal;

    private String tenantId;

    private Long parentId;

    @TableField(exist = false)
    private List<Tree<Long, AgentMessageEntity>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }

    public String obtainUserId() {
        return Strings.CS.replace(principal, CacheProperties.DEFAULT_SEPARATOR, CastUtils.UNDERSCORE);
    }
}