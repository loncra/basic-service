package io.github.loncra.basic.service.message.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.body.site.SiteMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.SiteMessageEntity;
import io.github.loncra.basic.service.message.server.resolver.support.SiteMessageSenderResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.annotation.Idempotent;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 站内信消息管理
 *
 * @author maurice
 * @see SiteMessageEntity
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("site")
@Plugin(
        name = "站内信消息",
        id = "site",
        parent = "message",
        authority = "perms[message_server_site:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SiteMessageController {

    private final SiteMessageSenderResolver siteMessageSender;

    /**
     * 获取站内信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     *
     * @return REST 响应结果
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[message_server_site:page]')")
    public Page<SiteMessageEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<SiteMessageEntity> query = siteMessageSender
                .getSiteMessageService()
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return siteMessageSender.getSiteMessageService().findTotalPage(pageRequest, query);
    }

    /**
     * 获取站内信消息
     *
     * @param id 站内信消息主键 ID
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[message_server_site:get]')")
    public SiteMessageEntity get(
            @PathVariable
            Long id
    ) {
        return siteMessageSender.getSiteMessageService().get(id);
    }

    /**
     * 删除站内信消息
     *
     * @param ids 站内信消息主键 ID 集合
     *
     * @return REST 响应结果
     */
    @DeleteMapping
    @Plugin(name = "删除信息", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[message_server_site:delete]')")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        siteMessageSender.getSiteMessageService().deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    @PostMapping("send")
    @Plugin(name = "发送站内信", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[message_server_site:send]')")
    @Idempotent(key = "net:hxaj:message:idempotent:site:send:[#body.principal]")
    public RestResult<Object> send(
            @RequestBody
            SiteMessageBody body
    ) {
        List<String> nonValidate = body
                .getToUsers()
                .stream()
                .filter(user -> !ResourceSourceEnum.validate(user))
                .toList();
        SystemException.isTrue(nonValidate.isEmpty(), "用户格式存在不规范内容 " + nonValidate + "修改正确后再提交数据");
        return siteMessageSender.sendMessage(Collections.singletonList(body));
    }

    @GetMapping("read/{id:\\d+}")
    @PreAuthorize("isAuthenticated()")
    public RestResult<SiteMessageEntity> read(
            @PathVariable
            Long id,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return RestResult.ofSuccess(siteMessageSender.getSiteMessageService().read(id, token));
    }

    /**
     * 阅读站内信
     *
     * @param types 站内信类型
     *
     * @return REST 响应结果
     */
    @PostMapping("read/all")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Void> readAll(
            @RequestParam(required = false)
            List<Long> types,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        siteMessageSender.getSiteMessageService().read(types, token);
        return RestResult.of("标记所有为已读成功");
    }

    /**
     * 删除已读消息
     *
     * @param types           消息类型
     * @param securityContext spring 安全上下文
     *
     * @return REST 响应结果
     */
    @DeleteMapping("read/delete")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Void> deleteRead(
            @RequestParam(required = false)
            List<Long> types,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        siteMessageSender.getSiteMessageService().deleteRead(types, token);
        return RestResult.of("删除所有已读信息成功");

    }

    @GetMapping("read/count")
    @PreAuthorize("isAuthenticated()")
    public RestResult<Long> countRead(
            @RequestParam
            String batchId
    ) {
        long count = siteMessageSender
                .getSiteMessageService()
                .lambdaQuery()
                .eq(SiteMessageEntity::getBatchId, batchId)
                .eq(SiteMessageEntity::getReadable, YesOrNo.No.getValue())
                .count();

        return RestResult.ofSuccess(count);
    }

    /**
     * 按类型分组获取站内信未读数量
     *
     * @return REST 响应结果
     */
    @GetMapping("unreadQuantity")
    @PreAuthorize("isAuthenticated()")
    public Map<Integer, Long> unreadQuantity(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return siteMessageSender.getSiteMessageService().countUnreadQuantity(token);
    }
}
