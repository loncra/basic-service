package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.framework.security.entity.RoleAuthority;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnterpriseMemberResponseBody extends EnterpriseMemberEntity {
    @Serial
    private static final long serialVersionUID = -9124524945119002927L;

    private List<RoleAuthority> roles = new LinkedList<>();
}
