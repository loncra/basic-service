package io.github.loncra.basic.service.auth.server.domain.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.framework.security.entity.RoleAuthority;
import lombok.Data;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {"principal", "roleIds"})
public class EnterpriseInvitationResponse extends EnterpriseInvitationEntity {

    @Serial
    private static final long serialVersionUID = -2749594157792147005L;

    private EnterpriseMemberEntity member;

    private List<RoleAuthority> roles = new LinkedList<>();
}
