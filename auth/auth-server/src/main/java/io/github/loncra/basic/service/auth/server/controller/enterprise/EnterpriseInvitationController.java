package io.github.loncra.basic.service.auth.server.controller.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseInvitationEntity;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseInvitationService;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * tb_enterprise_invitation 的控制器
 *
 * @see EnterpriseInvitationEntity
 *
 * @author maurice.chen
 *
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
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see EnterpriseInvitationEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:page]')")
    public Page<EnterpriseInvitationEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<EnterpriseInvitationEntity> query = enterpriseInvitationService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return enterpriseInvitationService.findPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see EnterpriseInvitationEntity
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:get]')")
    @Plugin(name = "查看明细")
    public EnterpriseInvitationEntity get(@PathVariable Integer id) {
        return enterpriseInvitationService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see EnterpriseInvitationEntity
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "保存或添加信息")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_invitation:save]')")
    public RestResult<Long> save(@Valid @RequestBody EnterpriseInvitationEntity entity) {
        enterpriseInvitationService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
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
