package io.github.loncra.basic.service.auth.server.domain.metdata;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.basic.service.commons.enumerate.AuditTypeEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnterpriseInvitationMetadata extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 249148732848045964L;

    /**
     * 邀请状态
     */
    private EnterpriseInvitationStatusEnum status = EnterpriseInvitationStatusEnum.EXECUTION;

    /**
     * 审核类型
     */
    private AuditTypeEnum auditType;

    /**
     * 过期时间
     */
    private Instant expirationTime;

    /**
     * 副标题
     */
    private String subTitle;

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
}
