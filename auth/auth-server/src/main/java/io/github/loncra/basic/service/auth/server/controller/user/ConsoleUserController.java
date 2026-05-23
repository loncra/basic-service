package io.github.loncra.basic.service.auth.server.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.service.user.console.ConsoleUserService;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ImportExportTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户管理
 *
 * @author maurice.chen
 * @see ConsoleUserEntity
 **/
@RestController
@RequestMapping("console/user")
@Plugin(
        name = "员工管理",
        id = "console_user",
        parent = "organization",
        authority = "perms[auth_server_console_user:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class ConsoleUserController {

    private final ConsoleUserService consoleUserService;

    @PostMapping("export")
    @Plugin(name = "导出查询结果")
    @PreAuthorize("hasAuthority('perms[auth_server_console_user:export]')")
    public RestResult<?> export(
            HttpServletRequest request,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());

        ExportDataMetadata dto =  new ExportDataMetadata();

        dto.setFilename(ImportExportTypeEnum.CONSOLE_USER.getName() + CastUtils.UNDERSCORE + System.currentTimeMillis() +  SystemConstants.EXCEL_SUFFIX_NAME);
        dto.setType(ImportExportTypeEnum.CONSOLE_USER);
        dto.getMetadata().put(SystemConstants.QUERY_KEY, request.getParameterMap());
        dto.setPrincipal(token.getName());

        consoleUserService.export(dto);

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
    @PreAuthorize("hasAuthority('perms[auth_server_console_user:page]')")
    public Page<ConsoleUserEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {

        QueryWrapper<ConsoleUserEntity> query = consoleUserService
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return consoleUserService.findTotalPage(pageRequest, query);
    }

    /**
     * 获取明细
     *
     * @param id 主键值
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("hasRole('FEIGN') or hasAuthority('perms[auth_server_console_user:get]')")
    @Plugin(name = "查看明细")
    public ConsoleUserEntity get(
            @PathVariable
            Long id
    ) {

        return consoleUserService.get(id);
    }

    /**
     * 添加或保存信息
     *
     * @param entity 数据请求体
     *
     * @return REST 响应结果
     */
    @PutMapping
    @Plugin(name = "添加或保存信息", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[auth_server_console_user:save]')")
    public RestResult<Long> save(
            @Valid
            @RequestBody
            ConsoleUserEntity entity
    ) {
        consoleUserService.save(entity);

        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除信息
     *
     * @param ids 系统用户主键 ID 集合
     *
     * @return REST 响应结果
     */
    @DeleteMapping
    @Plugin(name = "删除信息", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[auth_server_console_user:delete]')")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {

        consoleUserService.deleteById(ids, false);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 判断登录账户是否唯一
     *
     * @param username 登录账户
     *
     * @return REST 响应结果
     */
    @GetMapping("unique/username/{username}")
    @PreAuthorize("isAuthenticated()")
    public Boolean isUsernameUnique(
            @PathVariable
            String username
    ) {
        return !consoleUserService.lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getUsername, username)
                .exists();
    }

    /**
     * 判断邮件手机号码
     *
     * @param phoneNumber 电子邮件
     *
     * @return REST 响应结果
     */
    @GetMapping("unique/phoneNumber/{phoneNumber}")
    @PreAuthorize("isAuthenticated()")
    public Boolean isPhoneNumberUnique(
            @PathVariable
            String phoneNumber
    ) {
        return !consoleUserService.lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getPhoneNumber, phoneNumber)
                .exists();
    }

    /**
     * 判断邮件是否唯一
     *
     * @param email 电子邮件
     *
     * @return REST 响应结果
     */
    @GetMapping("unique/email/{email}")
    @PreAuthorize("isAuthenticated()")
    public Boolean isEmailUnique(
            @PathVariable
            String email
    ) {
        return !consoleUserService.lambdaQuery()
                .select(ConsoleUserEntity::getId)
                .eq(ConsoleUserEntity::getEmail, email)
                .exists();
    }
}
