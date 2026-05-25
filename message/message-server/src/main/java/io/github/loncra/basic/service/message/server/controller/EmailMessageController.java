package io.github.loncra.basic.service.message.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.body.email.EmailMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.EmailMessageEntity;
import io.github.loncra.basic.service.message.server.resolver.support.EmailMessageSenderResolver;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.annotation.Idempotent;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 邮件消息管理
 *
 * @author maurice
 * @see EmailMessageEntity
 * @since 2020-04-06 10:16:10
 */
@RestController
@RequestMapping("email")
@Plugin(
        name = "邮件消息",
        id = "email",
        parent = "message",
        authority = "perms[message_server_email:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class EmailMessageController {

    private final EmailMessageSenderResolver emailMessageSender;

    /**
     * 获取邮件消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     *
     * @return REST 响应结果
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[message_server_email:page]')")
    public Page<EmailMessageEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {

        QueryWrapper<EmailMessageEntity> query = emailMessageSender
                .getEmailMessageService()
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        return emailMessageSender.getEmailMessageService().findTotalPage(pageRequest, query);
    }

    /**
     * 获取邮件消息
     *
     * @param id 邮件消息主键 ID
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[message_server_email:get]')")
    @Plugin(name = "查看明细")
    public EmailMessageEntity get(
            @PathVariable
            Long id
    ) {
        return emailMessageSender.getEmailMessageService().get(id);
    }

    /**
     * 删除邮件消息
     *
     * @param ids 邮件消息主键 ID 集合
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[message_server_email:delete]')")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        emailMessageSender.getEmailMessageService().deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 发送消息
     *
     * @param body 请求
     *
     * @return REST 响应结果
     */
    @OperationDataTrace
    @PostMapping("send")
    @Plugin(name = "发送信息")
    @PreAuthorize("hasAuthority('perms[message_server_email:send]')")
    @Idempotent(key = "net:hxaj:message:idempotent:email:send:[#body.principal]")
    public RestResult<Object> send(
            @RequestBody
            EmailMessageBody body
    ) {
        List<String> nonValidate = body
                .getToEmails()
                .stream()
                .filter(email -> !ResourceSourceEnum.validate(email) && !Pattern.matches(SystemConstants.EMAIL_REGULAR_EXPRESSION, email))
                .toList();
        SystemException.isTrue(nonValidate.isEmpty(), "邮箱存在不规范内容 " + nonValidate + "修改正确后再提交数据");
        return emailMessageSender.sendMessage(Collections.singletonList(body));
    }

}
