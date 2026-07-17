package io.github.loncra.basic.service.ai.server.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


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
@TableName("tb_agent_message")
@EqualsAndHashCode(callSuper = true)
public class AgentMessageEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -5121909839700678113L;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 对话 id
     */
    private Long agentConversationId;

    /**
     * 内容
     */
    private String content;

    /**
     * 媒体内容
     */
    private String media;

    /**
     * 元数据信息
     */
    private String metadata;

    /**
     * 当前用户
     */
    private String principal;

    /**
     * 租户 id
     */
    private String tenantId;

}