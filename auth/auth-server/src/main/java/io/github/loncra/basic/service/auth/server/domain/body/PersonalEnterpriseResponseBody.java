package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberInvitationEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
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
     * 成员状态
     */
    private EnterpriseMemberInvitationEnum status;
}
