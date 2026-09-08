package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseInvitationDetailResponse extends EnterpriseInvitationResponse {

    @Serial
    private static final long serialVersionUID = -2749594157792147005L;

    private EnterpriseEntity enterprise;

    private EnterpriseMemberEntity invitee;
}
