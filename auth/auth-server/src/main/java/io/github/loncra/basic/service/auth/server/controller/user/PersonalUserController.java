package io.github.loncra.basic.service.auth.server.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.body.PersonalUserRegisterRequestBody;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ImportExportTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.captcha.ReceivingTargetSimpleCaptcha;
import io.github.loncra.framework.captcha.filter.CaptchaVerificationFilter;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.audit.Auditable;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 *
 * 个人用户管理
 *
 * @see PersonalUserEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-28 09:46:07
 */
@RestController
@RequestMapping("personal/user")
@Plugin(
    name = "个人用户管理",
    id = "personal_user",
    parent = "system",
    authority = "perms[auth_server_personal_user:page]",
    type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
    sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class PersonalUserController {

    private final PersonalUserService personalUserService;

    @Auditable
    @PostMapping("register")
    public RestResult<Long> register(
            @Valid
            @ModelAttribute
            PersonalUserRegisterRequestBody body
    ) {
        ReceivingTargetSimpleCaptcha captcha = CastUtils.convertValue(
                SpringMvcUtils.getRequestAttribute(CaptchaVerificationFilter.ATTR_NAME),
                ReceivingTargetSimpleCaptcha.class
        );
        PersonalUserEntity user = personalUserService.register(body, captcha);
        return RestResult.ofSuccess("注册成功", user.getId());
    }

    @PostMapping("export")
    @Plugin(name = "导出查询结果")
    @PreAuthorize("hasAuthority('perms[auth_server_personal_user:export]')")
    public RestResult<?> export(
            HttpServletRequest request,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        ExportDataMetadata dto = UserExportSupport.createExportData(request, token, ImportExportTypeEnum.PERSONAL_USER);
        personalUserService.export(dto);

        return RestResult.of("执行导出成功, 请耐心等待后台导出完成后即可下载导出文件");
    }

    /**
     * 获取分页
     *
     * @param pageRequest 分页信息
     * @param request  http servlet request
     *
     * @return 分页实体
     *
     * @see PersonalUserEntity
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[auth_server_personal_user:page]')")
    public Page<PersonalUserEntity> page(PageRequest pageRequest, HttpServletRequest request) {
        QueryWrapper<PersonalUserEntity> query = personalUserService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return personalUserService.findPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键 ID
     *
     * @return REST 响应结果
     *
     * @see PersonalUserEntity
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[auth_server_personal_user:get]')")
    @Plugin(name = "查看明细")
    public PersonalUserEntity get(@RequestParam Integer id) {
        return personalUserService.get(id);
    }

    /**
     * 保存数据
     *
     * @param entity 数据请求体
     *
     * @see PersonalUserEntity
     */
    @PutMapping
    @OperationDataTrace
    @PreAuthorize("hasAuthority('perms[auth_server_personal_user:save]')")
    @Plugin(name = "保存或添加信息")
    public RestResult<Long> save(@Valid @RequestBody PersonalUserEntity entity) {
        personalUserService.save(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据
     *
     * @param ids 主键 ID 值集合
     *
     * @see PersonalUserEntity
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[auth_server_personal_user:delete]')")
    public RestResult<Void> delete(@RequestParam List<Long> ids) {
        personalUserService.deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

}
