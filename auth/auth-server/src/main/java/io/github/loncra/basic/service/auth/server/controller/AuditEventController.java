package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.DateUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.StringIdEntity;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.audit.ExtendAuditEventRepository;
import io.github.loncra.framework.security.audit.IdAuditEvent;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.config.ControllerAuditProperties;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作数据留痕管理
 *
 * @author maurice
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("audit/event")
public class AuditEventController {

    private final ControllerAuditProperties controllerAuditProperties;

    private final AuditEventRepository auditEventRepository;

    @PostMapping("authentication/page")
    @PreAuthorize("hasAuthority('perms[auth_server_audit_event:authentication]')")
    @Plugin(id = "login_log", name = "登录日志查询", parent = "log", type = ResourceTypeEnum.RESOURCE_MENU_TYPE, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Object authenticationPage(
            PageRequest pageRequest,
            @RequestParam(required = false)
            String principal,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Date after
    ) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(principal)) {
            query.put(IdAuditEvent.PRINCIPAL_FIELD_NAME, principal);
        }

        if (auditEventRepository instanceof ExtendAuditEventRepository extendAuditEventRepository) {
            query.put("filter_[type_eq]", SystemConstants.AUDIT_EVENT_AUTHENTICATION_TYPE_NAME);
            return extendAuditEventRepository.findPage(pageRequest, after.toInstant(), query);
        } else {
            return auditEventRepository.find(principal, after.toInstant(), SystemConstants.AUDIT_EVENT_AUTHENTICATION_TYPE_NAME);
        }
    }

    @PostMapping("audit/page")
    @PreAuthorize("hasAuthority('perms[auth_server_audit_event:audit]')")
    @Plugin(
            id = "audit_log",
            name = "审计日志查询",
            parent = "log",
            remark = "非数据库操作数据留痕",
            type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
            sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
    )
    public Object auditPage(
            PageRequest pageRequest,
            @RequestParam(required = false)
            String principal,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Date after,
            HttpServletRequest request
    ) {
        if (auditEventRepository instanceof ExtendAuditEventRepository extendAuditEventRepository) {
            Map<String, Object> filter = HttpRequestParameterMapUtils.castArrayValueMapToObjectValueMap(request.getParameterMap());
            Map<String, Object> query = new LinkedHashMap<>(filter);
            query.put("filter_[type_eq]", controllerAuditProperties.getControllerAuditName());
            return extendAuditEventRepository.findPage(pageRequest, after.toInstant(), query);
        } else {
            return auditEventRepository.find(principal, after.toInstant(), controllerAuditProperties.getOperationDataTraceAuditName());
        }
    }

    @PostMapping("operationDataTrace/page")
    @PreAuthorize("hasAuthority('perms[auth_server_audit_event:operation_data_trace]') or isAuthenticated()")
    @Plugin(
            id = "operation_log",
            name = "操作日志查询",
            parent = "log",
            remark = "数据库操作数据留痕",
            type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
            sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.PERSONAL_SOURCE_VALUE}
    )
    public Object operationDataTracePage(
            PageRequest pageRequest,
            @RequestParam(required = false)
            String principal,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Date after,
            HttpServletRequest request,
            @CurrentSecurityContext SecurityContext securityContext
    ) {
        AuditAuthenticationToken auditAuthenticationToken = CastUtils.cast(securityContext.getAuthentication());
        if (ResourceSourceEnum.PERSONAL_SOURCE_VALUE.equals(auditAuthenticationToken.getType())) {
            principal = auditAuthenticationToken.getName();
        }
        if (auditEventRepository instanceof ExtendAuditEventRepository extendAuditEventRepository) {
            Map<String, Object> filter = HttpRequestParameterMapUtils.castArrayValueMapToObjectValueMap(request.getParameterMap());
            Map<String, Object> query = new LinkedHashMap<>(filter);
            query.put("filter_[type_eq]", controllerAuditProperties.getOperationDataTraceAuditName());
            return extendAuditEventRepository.findPage(pageRequest, after.toInstant(), query);
        } else {
            return auditEventRepository.find(principal, after.toInstant(), controllerAuditProperties.getOperationDataTraceAuditName());
        }
    }

    /**
     * 获取明细
     *
     * @param id    主键 id
     * @param after 数据发生时间
     *
     * @return REST 响应结果
     */
    @GetMapping({"authentication/{id}", "operationDataTrace/{id}"})
    @Plugin(name = "查看明细", parent = "log", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    @PreAuthorize("hasAuthority('perms[auth_server_audit_event:get]')")
    public AuditEvent get(
            @PathVariable(required = false)
            String id,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Date after
    ) {
        SystemException.isTrue(ExtendAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass()), "auditEventRepository 非 ExtendAuditEventRepository 实现，不支持调用此接口");

        ExtendAuditEventRepository extendAuditEventRepository = CastUtils.cast(auditEventRepository);

        StringIdEntity stringIdEntity = new StringIdEntity();
        stringIdEntity.setId(id);
        stringIdEntity.setCreationTime(after.toInstant());
        return extendAuditEventRepository.get(stringIdEntity);
    }

    @GetMapping("principal/activity")
    @PreAuthorize("isAuthenticated()")
    public Object principalActivity(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {

        String principal = securityContext.getAuthentication().getName();
        Instant after = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        if (auditEventRepository instanceof ExtendAuditEventRepository extendAuditEventRepository) {
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("filter_[principal_eq]", principal);
            query.put("filter_[type_in]", List.of(controllerAuditProperties.getOperationDataTraceAuditName(), controllerAuditProperties.getControllerAuditName()));
            return extendAuditEventRepository.findPage(PageRequest.of(1000), LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(), query);
        } else {
            return auditEventRepository.find(principal, after, controllerAuditProperties.getOperationDataTraceAuditName());
        }
    }
}
