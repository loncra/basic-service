package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.commons.enumerate.AuditStatusEnum;
import io.github.loncra.framework.commons.enumerate.security.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonalEnterpriseResponseBody extends EnterpriseEntity {

    @Serial
    private static final long serialVersionUID = -4164143963009021828L;

    private EnterpriseMemberRoleEnum role;

    /**
     * 审核状态
     */
    private AuditStatusEnum auditStatus;

    /**
     * 用户状态
     */
    private UserStatus userStatus;
}
