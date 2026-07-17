package io.github.loncra.basic.service.ai.server.domain.entity.agent;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_agent_workspace - agent 工作空间</p>
 *
 * @author maurice.chen
 *
 * @since 2026-07-15 09:32:24
 */
@Data
@NoArgsConstructor
@Alias("agentWorkspace")
@TableName("tb_agent_workspace")
@EqualsAndHashCode(callSuper = true)
public class AgentWorkspaceEntity extends LongVersionEntity<Integer> implements TenantEntity<String>, AuditPrincipal {

    @Serial
    private static final long serialVersionUID = 4911256890890419381L;

    /**
     * 名称
     */
    private String name;

    /**
     * 租户 id
     */
    private String tenantId;

    /**
     * 当前用户
     */
    private String principal;

}