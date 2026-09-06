package io.github.loncra.basic.service.auth.server.controller.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseMemberService;
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
 * tb_enterprise_member 的控制器
 *
 * @author maurice.chen
 * @see EnterpriseMemberEntity
 * @since 2026-09-04 10:09:44
 */
@RestController
@RequestMapping("enterprise/member")
@Plugin(
        name = "成员管理",
        id = "enterprise_member",
        parent = "organization",
        authority = "perms[auth_server_enterprise_member:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.ENTERPRISE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class EnterpriseMemberController {

    private final EnterpriseMemberService enterpriseMemberService;

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     * @see EnterpriseMemberEntity
     */
    @PostMapping
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_member:page]')")
    public Page<EnterpriseMemberEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<EnterpriseMemberEntity> query = enterpriseMemberService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        Page<EnterpriseMemberEntity> result = enterpriseMemberService.findTotalPage(pageRequest, query);
        result.getElements().forEach(enterpriseMemberService::setPersonalUser);
        return result;
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     * @return REST 响应结果
     * @see EnterpriseMemberEntity
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_member:get]')")
    @Plugin(name = "查看明细")
    public EnterpriseMemberEntity get(
            @PathVariable
            Long id,
            @RequestParam(required = false, defaultValue = "true")
            boolean loadPersonalUser
    ) {
        EnterpriseMemberEntity result = enterpriseMemberService.get(id);
        if (loadPersonalUser) {
            enterpriseMemberService.setPersonalUser(result);
        }
        return result;
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     * @see EnterpriseMemberEntity
     */
    @PutMapping
    @OperationDataTrace
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_member:save]')")
    @Plugin(name = "保存或添加信息")
    public RestResult<Long> save(@Valid @RequestBody EnterpriseMemberEntity entity) {
        enterpriseMemberService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     * @see EnterpriseMemberEntity
     */
    @DeleteMapping
    @OperationDataTrace
    @PreAuthorize("hasAuthority('perms[auth_server_enterprise_member:delete]')")
    @Plugin(name = "删除信息")
    public RestResult<Void> delete(@RequestParam List<Long> ids) {
        enterpriseMemberService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }
}
