package io.github.loncra.basic.service.auth.server.controller.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.controller.user.UserExportSupport;
import io.github.loncra.basic.service.auth.server.domain.body.PersonalEnterpriseResponseBody;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseService;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ImportExportTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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

    @PostMapping("export")
    @Plugin(name = "导出查询结果")
    @PreAuthorize("hasAuthority('perms[auth_server_console_user:export]')")
    public RestResult<?> export(
            HttpServletRequest request,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        ExportDataMetadata dto = UserExportSupport.createExportData(request, token, ImportExportTypeEnum.ENTERPRISE);
        enterpriseService.export(dto);

        return RestResult.of("执行导出成功, 请耐心等待后台导出完成后即可下载导出文件");
    }

    /**
     * 获取分页
     *
     * @param pageRequest 分页请求
     * @param request     http 请求
     *
     * @return REST 响应结果
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise:page]')")
    public Page<EnterpriseEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {

        QueryWrapper<EnterpriseEntity> query = enterpriseService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return enterpriseService.findTotalPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键值
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public EnterpriseEntity get(
            @PathVariable
            Long id
    ) {

        return enterpriseService.get(id);
    }

    @PutMapping
    @OperationDataTrace
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "添加或保存信息", sources = {ResourceSourceEnum.PERSONAL_SOURCE_VALUE})
    public RestResult<Long> save(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @Valid
            @RequestBody
            EnterpriseEntity body
    ) {
        EnterpriseEntity organization = enterpriseService.save(
                CastUtils.cast(securityContext.getAuthentication()),
                body
        );
        return RestResult.ofSuccess("创建企业成功", organization.getId());
    }

    @GetMapping("my")
    @PreAuthorize("isAuthenticated()")
    public List<PersonalEnterpriseResponseBody> my(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return enterpriseService.findByPrincipal(token.getName());
    }

    @PutMapping("switch")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<Long> switchEnterprise(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(required = false)
            Long enterpriseId
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        if (Objects.isNull(enterpriseId)) {
            enterpriseService.switchByEnterprise(token, null);
        } else{
            enterpriseService.switchByEnterpriseId(token,enterpriseId);
        }
        return RestResult.ofSuccess("切换空间成功", enterpriseId);
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

    @DeleteMapping("members/leave/{organizationId:\\d+}")
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
