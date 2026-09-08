package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.server.domain.metdata.EnterpriseInvitationMetadata;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
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
public class EnterpriseInvitationEntity extends EnterpriseInvitationMetadata implements TenantEntity<String>, AuditPrincipal {

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
     * 角色 id
     */
    @NotNull
    @JsonCollectionGenericType(Long.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<Long> roleIds = new LinkedHashSet<>();

    /**
     * 租户 id
     */
    private String tenantId;
}
