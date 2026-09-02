package io.github.loncra.basic.service.auth.server.controller.organization;

import io.github.loncra.basic.service.auth.server.service.organization.OrganizationService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业管理
 *
 * @author maurice.chen
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("switch")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Long> switchOrganization(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(required = false)
            Long organizationId
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        organizationService.switchOrganization(token, organizationId);
        return RestResult.ofSuccess("切换空间成功", organizationId);
    }
}
