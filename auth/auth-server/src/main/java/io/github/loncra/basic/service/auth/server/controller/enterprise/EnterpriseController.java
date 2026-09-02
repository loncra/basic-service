package io.github.loncra.basic.service.auth.server.controller.enterprise;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业管理
 *
 * @author maurice.chen
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("enterprise")
@Plugin(
        name = "企业管理",
        id = "enterprise",
        parent = "member",
        authority = "perms[auth_server_enterprise:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.PERSONAL_SOURCE_VALUE}
)
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    @PostMapping
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Long> create(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            String name,
            @RequestParam(required = false)
            String remark
    ) {
        EnterpriseEntity organization = enterpriseService.create(
                CastUtils.cast(securityContext.getAuthentication()),
                name,
                remark
        );
        return RestResult.ofSuccess("创建企业成功", organization.getId());
    }

    @GetMapping("my")
    @PreAuthorize("isFullyAuthenticated()")
    public List<EnterpriseEntity> my(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        return enterpriseService.findByOrganizationIds(
                CastUtils.cast(securityContext.getAuthentication())
        );
    }

    @PostMapping("switch")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Long> switchOrganization(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(required = false)
            Long organizationId
    ) {
        enterpriseService.switchOrganization(
                CastUtils.cast(securityContext.getAuthentication()),
                organizationId
        );
        return RestResult.ofSuccess("切换空间成功", organizationId);
    }

    @PostMapping("invitations/{organizationId:\\d+}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<String> invite(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @PathVariable
            Long organizationId,
            @RequestParam
            String phoneNumber
    ) {
        EnterpriseInvitationEntity invitation = enterpriseService.invite(
                CastUtils.cast(securityContext.getAuthentication()),
                organizationId,
                phoneNumber
        );
        return RestResult.ofSuccess("邀请成员成功", invitation.getCode());
    }

    @PostMapping("invitations/accept/{code}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Void> acceptInvitation(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @PathVariable
            String code
    ) {
        enterpriseService.acceptInvitation(
                CastUtils.cast(securityContext.getAuthentication()),
                code
        );
        return RestResult.of("接受企业邀请成功");
    }

    @GetMapping("members/{organizationId:\\d+}")
    @PreAuthorize("isFullyAuthenticated()")
    public List<EnterpriseMemberEntity> members(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @PathVariable
            Long organizationId
    ) {
        return enterpriseService.findMembers(
                CastUtils.cast(securityContext.getAuthentication()),
                organizationId
        );
    }

    @DeleteMapping("members/{organizationId:\\d+}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Void> removeMember(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @PathVariable
            Long organizationId,
            @RequestParam
            String principal
    ) {
        enterpriseService.removeMember(
                CastUtils.cast(securityContext.getAuthentication()),
                organizationId,
                principal
        );
        return RestResult.of("移除企业成员成功");
    }

    @DeleteMapping("members/{organizationId:\\d+}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Void> leave(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @PathVariable
            Long organizationId
    ) {
        enterpriseService.leave(
                CastUtils.cast(securityContext.getAuthentication()),
                organizationId
        );
        return RestResult.of("退出企业成功");
    }
}
