package io.github.loncra.basic.service.message.server.controller.sms;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsBalanceMetadata;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsMessageBody;
import io.github.loncra.basic.service.message.server.domain.entity.SmsMessageEntity;
import io.github.loncra.basic.service.message.server.resolver.support.SmsMessageSenderResolver;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsChannelSender;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信消息管理
 *
 * @author maurice
 * @see SmsMessageEntity
 * @since 2020-04-06 10:16:10
 */
@Slf4j
@RestController
@RequestMapping("sms")
@Plugin(
        name = "短信消息",
        id = "sms",
        parent = "message",
        authority = "perms[message_server_sms:page]",
        type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SmsMessageController {

    private final SmsMessageSenderResolver smsMessageSender;

    /**
     * 获取短信消息分页信息
     *
     * @param pageRequest 分页信息
     * @param request     过滤条件
     *
     * @return REST 响应结果
     */
    @PostMapping("page")
    @PreAuthorize("hasAuthority('perms[message_server_sms:page]')")
    public Page<SmsMessageEntity> page(
            PageRequest pageRequest,
            HttpServletRequest request
    ) {
        QueryWrapper<SmsMessageEntity> query = smsMessageSender
                .getSmsMessageService()
                .getQueryGenerator()
                .getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);
        return smsMessageSender.getSmsMessageService().findTotalPage(pageRequest, query);
    }

    /**
     * 获取短信消息
     *
     * @param id 短信消息主键 ID
     *
     * @return REST 响应结果
     */
    @GetMapping("{id:\\d+}")
    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[message_server_sms:get]')")
    public SmsMessageEntity get(
            @PathVariable
            Long id
    ) {
        return smsMessageSender.getSmsMessageService().get(id);
    }

    /**
     * 删除短信消息
     *
     * @param ids 短信消息主键 ID 集合
     */
    @DeleteMapping
    @OperationDataTrace
    @Plugin(name = "删除信息")
    @PreAuthorize("hasAuthority('perms[message_server_sms:delete]')")
    public RestResult<Void> delete(
            @RequestParam
            List<Long> ids
    ) {
        smsMessageSender.getSmsMessageService().deleteById(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    /**
     * 获取短信余额
     *
     * @return REST 响应结果
     */
    @GetMapping("balance")
    @Plugin(name = "查看短信余额")
    @PreAuthorize("hasAuthority('perms[message_server_sms:balance]')")
    public RestResult<List<SmsBalanceMetadata>> balance() {
        return RestResult.ofSuccess(smsMessageSender
                                            .getSmsChannelSenderList()
                                            .stream()
                                            .map(SmsChannelSender::getBalance)
                                            .collect(Collectors.toList()));
    }

    /**
     * 发送短信
     *
     * @param body 短信实体
     *
     * @return REST 响应结果
     */
    @PutMapping
    @OperationDataTrace
    @Plugin(name = "发送短信")
    @PreAuthorize("hasAuthority('perms[message_server_sms:send]')")
    public RestResult<Object> send(
            @RequestBody
            SmsMessageBody body
    ) {
        return smsMessageSender.sendMessage(Collections.singletonList(body));
    }

}
