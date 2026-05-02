package io.github.loncra.basic.service.auth.server.controller;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.DateUtils;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.StringIdEntity;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.config.OperationDataTraceProperties;
import io.github.loncra.framework.security.audit.ExtendAuditEventRepository;
import io.github.loncra.framework.security.audit.IdAuditEvent;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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
@ConditionalOnBean(ExtendAuditEventRepository.class)
public class AuditEventController {

    public static final String OPERATION_DATA_TRACE_PAGE_ID = "operationDataTracePage";
    public static final String AUTHENTICATION_PAGE_ID = "authenticationPage";

    private final OperationDataTraceProperties operationDataTraceProperties;

    private final ExtendAuditEventRepository operationDataTraceRepository;

    @PostMapping("login")
    @PreAuthorize("hasAuthority('auth_server_audit_event:login')")
    @Plugin(id = "login_log", name = "登录日志查询", parent = "log", type = SystemConstants.RESOURCE_MENU_TYPE, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<AuditEvent> authenticationPage(
            PageRequest pageRequest,
            @RequestParam(required = false)
            String principal,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Instant after
    ) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put(IdAuditEvent.TYPE_FIELD_NAME, SystemConstants.AUDIT_EVENT_AUTHENTICATION_TYPE_NAME);
        if (StringUtils.isNotBlank(principal)) {
            query.put(IdAuditEvent.PRINCIPAL_FIELD_NAME, principal);
        }
        query.put(IdEntity.ID_FIELD_NAME, AUTHENTICATION_PAGE_ID);
        return operationDataTraceRepository.findPage(pageRequest, after, query);
    }

    @PostMapping("operationDataTrace")
    @PreAuthorize("hasAuthority('auth_server_audit_event:operation_data_trace')")
    @Plugin(id = "operation_log", name = "操作日志查询", parent = "log", type = SystemConstants.RESOURCE_MENU_TYPE, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Page<AuditEvent> operationDataTracePage(
            PageRequest pageRequest,
            @RequestParam(required = false)
            String target,
            @RequestParam(required = false)
            String type,
            @RequestParam(required = false)
            String principal,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Instant after,
            @RequestParam(required = false)
            String entityId
    ) {
        Map<String, Object> query = new LinkedHashMap<>();
        String auditType = operationDataTraceProperties.getAuditPrefixName() + CastUtils.UNDERSCORE;
        if (StringUtils.isNotBlank(type)) {
            auditType += type + CastUtils.UNDERSCORE + DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL;
        }
        else {
            auditType += DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL;
        }

        query.put(IdAuditEvent.TYPE_FIELD_NAME, auditType);
        if (StringUtils.isNotBlank(target) && StringUtils.isNotBlank(entityId)) {
            query.put(SystemConstants.ES_OPERATION_DATE_TARGET_NAME, target);
            query.put(SystemConstants.ES_OPERATION_DATE_ENTITY_ID_NAME, entityId);
        }

        if (StringUtils.isNotBlank(principal)) {
            query.put(IdAuditEvent.PRINCIPAL_FIELD_NAME, principal);
        }

        query.put(IdEntity.ID_FIELD_NAME, OPERATION_DATA_TRACE_PAGE_ID);

        return operationDataTraceRepository.findPage(pageRequest, after, query);
    }

    /**
     * 获取明细
     *
     * @param id    主键 id
     * @param after 数据发生时间
     *
     * @return REST 响应结果
     */
    @GetMapping("/{id}")
    @Plugin(name = "查看明细", parent = "log", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    @PreAuthorize("hasAuthority('auth_server_audit_event:get')")
    public AuditEvent get(
            @PathVariable(required = false)
            String id,
            @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_TIME_FORMATTER_PATTERN)
            @RequestParam
            Instant after
    ) {

        StringIdEntity stringIdEntity = new StringIdEntity();
        stringIdEntity.setId(id);
        stringIdEntity.setCreationTime(after);
        return operationDataTraceRepository.get(stringIdEntity);
    }
}
