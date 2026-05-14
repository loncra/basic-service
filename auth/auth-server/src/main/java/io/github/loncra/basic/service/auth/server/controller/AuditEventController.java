package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.DateUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.StringIdEntity;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.config.OperationDataTraceProperties;
import io.github.loncra.framework.security.audit.ExtendAuditEventRepository;
import io.github.loncra.framework.security.audit.IdAuditEvent;
import io.github.loncra.framework.security.plugin.Plugin;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
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

    public static final String OPERATION_DATA_TRACE_PAGE_ID = "operationDataTracePage";
    public static final String AUTHENTICATION_PAGE_ID = "authenticationPage";

    private final OperationDataTraceProperties operationDataTraceProperties;

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
        query.put(IdAuditEvent.TYPE_FIELD_NAME, SystemConstants.AUDIT_EVENT_AUTHENTICATION_TYPE_NAME);
        if (StringUtils.isNotBlank(principal)) {
            query.put(IdAuditEvent.PRINCIPAL_FIELD_NAME, principal);
        }
        query.put(IdEntity.ID_FIELD_NAME, AUTHENTICATION_PAGE_ID);
        if (auditEventRepository instanceof ExtendAuditEventRepository extendAuditEventRepository) {
            return extendAuditEventRepository.findPage(pageRequest, after.toInstant(), query);
        } else {
            return auditEventRepository.find(principal, after.toInstant(), SystemConstants.AUDIT_EVENT_AUTHENTICATION_TYPE_NAME);
        }
    }

    @PostMapping("operationDataTrace/page")
    @PreAuthorize("hasAuthority('perms[auth_server_audit_event:operation_data_trace]')")
    @Plugin(id = "operation_log", name = "操作日志查询", parent = "log", type = ResourceTypeEnum.RESOURCE_MENU_TYPE, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Object operationDataTracePage(
            PageRequest pageRequest,
            @RequestParam(required = false)
            String type,
            @RequestParam(required = false)
            String principal,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Date after,
            HttpServletRequest request
    ) {

        Map<String, Object> query = new LinkedHashMap<>();
        String auditType = operationDataTraceProperties.getAuditPrefixName() + CastUtils.UNDERSCORE;
        if (StringUtils.isNotBlank(type)) {
            auditType += type;
        }

        if (auditEventRepository instanceof ExtendAuditEventRepository extendAuditEventRepository) {
            Map<String, Object> filter = HttpRequestParameterMapUtils.castArrayValueMapToObjectValueMap(request.getParameterMap());
            query.putAll(filter);
            query.put("filter_[type_rlike]", auditType);
            return extendAuditEventRepository.findPage(pageRequest, after.toInstant(), query);
        } else {
            return auditEventRepository.find(principal, after.toInstant(), auditType);
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
            Instant after
    ) {
        SystemException.isTrue(ExtendAuditEventRepository.class.isAssignableFrom(auditEventRepository.getClass()), "auditEventRepository 非 ExtendAuditEventRepository 实现，不支持调用此接口");

        ExtendAuditEventRepository extendAuditEventRepository = CastUtils.cast(auditEventRepository);

        StringIdEntity stringIdEntity = new StringIdEntity();
        stringIdEntity.setId(id);
        stringIdEntity.setCreationTime(after);
        return extendAuditEventRepository.get(stringIdEntity);
    }
}
