package io.github.loncra.basic.service.ai.server.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_agent_conversation", autoResultMap = true)
public class AgentConversationEntity extends LongVersionEntity<Integer> implements TenantEntity<String>, AuditPrincipal, Tree<Long, AgentConversationEntity> {

    @Serial
    private static final long serialVersionUID = -4801789299046545458L;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private AgentChatStatusEnum status;

    private String principal;

    private String tenantId;

    private AgentConversationTypeEnum type;

    private ExecuteStatus generateNameStatus;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    private Long parentId;

    /**
     * 最后使用的模型内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private ModelSettingMetadata lastModel;

    /**
     * 最后一次对话类型
     */
    private AgentChatTypeEnum lastChatType;

    @TableField(exist = false)
    private List<Tree<Long, AgentConversationEntity>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }

    public String getKey() {
        return getId().toString();
    }
}