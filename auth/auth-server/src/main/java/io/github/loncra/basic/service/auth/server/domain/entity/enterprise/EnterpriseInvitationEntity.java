package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationAuditEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>Table: tb_enterprise_invitation - 企业邀请表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("enterpriseInvitation")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_enterprise_invitation", autoResultMap = true)
public class EnterpriseInvitationEntity extends LongVersionEntity<Integer> implements TenantEntity<String>, AuditPrincipal {

    @Serial
    private static final long serialVersionUID = -6319209699259121659L;

    /**
     * 企业 id
     */
    private Long enterpriseId;

    /**
     * 邀请人
     */
    private String principal;

    /**
     * 邀请状态
     */
    private EnterpriseInvitationStatusEnum status = EnterpriseInvitationStatusEnum.EXECUTION;

    /**
     * 审核类型
     */
    private EnterpriseInvitationAuditEnum auditType;

    /**
     * 过期时间
     */
    private Instant expirationTime;

    /**
     * 角色 id
     */
    @NotNull
    @JsonCollectionGenericType(Long.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<Long> roleIds = new LinkedHashSet<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户 id
     */
    private String tenantId;
}
