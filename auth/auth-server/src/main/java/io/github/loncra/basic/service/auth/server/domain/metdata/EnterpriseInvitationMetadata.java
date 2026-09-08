package io.github.loncra.basic.service.auth.server.domain.metdata;

import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationAuditEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;

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
    private EnterpriseInvitationAuditEnum auditType;

    /**
     * 过期时间
     */
    private Instant expirationTime;

    /**
     * 副标题
     */
    private String subTitle;

    /**
     * 备注
     */
    private String remark;
}
