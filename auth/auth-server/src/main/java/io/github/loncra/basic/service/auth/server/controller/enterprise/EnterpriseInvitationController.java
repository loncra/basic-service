package io.github.loncra.basic.service.auth.server.controller.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.domain.metdata.EnterpriseInvitationMetadata;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseInvitationService;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.TotalPage;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * tb_enterprise_invitation 的控制器
 *
 * @author maurice.chen
 * @see EnterpriseInvitationEntity
 * @since 2026-09-04 10:16:19
 */
@RestController
@RequestMapping("enterprise/invitation")
@Plugin(
        name = "邀请管理",
        id = "enterprise_invitation",
        parent = "organization",
        authority = "perms[auth_server_enterprise_invitation:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class EnterpriseInvitationController {

    private final EnterpriseInvitationService enterpriseInvitationService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see EnterpriseInvitationEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:page]')")
    public Page<EnterpriseInvitationMetadata> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<EnterpriseInvitationEntity> query = enterpriseInvitationService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        TotalPage<EnterpriseInvitationEntity> result = enterpriseInvitationService.findTotalPage(pageRequest, query);
        List<EnterpriseInvitationMetadata> elements = result.getElements()
                .stream()
                .map(enterpriseInvitationService::convertResponseBody)
                .collect(Collectors.toCollection(LinkedList::new));
        return new TotalPage<>(pageRequest, elements, result.getTotalCount());
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     * @return REST 响应结果
     * @see EnterpriseInvitationEntity
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:get]')")
    @Plugin(name = "查看明细")
    public EnterpriseInvitationMetadata get(
            @PathVariable
            Integer id,
            @RequestParam(required = false, defaultValue = "true")
            boolean convertResponseBody
    ) {
        EnterpriseInvitationEntity result = enterpriseInvitationService.get(id);

        if (convertResponseBody) {
            return enterpriseInvitationService.convertResponseBody(result);
        }

        return result;
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     * @see EnterpriseInvitationEntity
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "保存或添加信息")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:save]')")
    public RestResult<Long> save(
            @Valid
            @RequestBody
            EnterpriseInvitationEntity entity,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        String enterpriseId = details.getMetadata()
                .get(PrincipalDetailsConstants.ENTERPRISE_ID_KEY)
                .toString();
        entity.setEnterpriseId(NumberUtils.toLong(enterpriseId));
        enterpriseInvitationService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     * @see EnterpriseInvitationEntity
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:delete]')")
    public RestResult<Void> delete(@RequestParam List<Integer> ids) {
        enterpriseInvitationService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
