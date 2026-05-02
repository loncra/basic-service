package io.github.loncra.basic.service.message.server.controller;


import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.api.domian.metadata.SmsSignMetadata;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsSignResponseBody;
import io.github.loncra.basic.service.message.server.resolver.support.SmsMessageSenderResolver;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsChannelSender;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短信签名管理
 *
 * @author mauricee.chen
 */
@Slf4j
@RestController
@RequestMapping("sms/sign")
@Plugin(
        name = "短信签名管理",
        id = "sms_sign",
        parent = "sms",
        authority = "perms[message_server_sms_sign:list]",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SmsSignController {

    private final SmsMessageSenderResolver smsMessageSender;

    @Plugin(name = "查看签名列表")
    @GetMapping("/{channel}")
    @PreAuthorize("hasAuthority('perms[message_server_sms_sign:list]')")
    public List<SmsSignResponseBody> list(
            @PathVariable(required = false)
            String channel
    ) {
        if (StringUtils.isNotEmpty(channel)) {
            return smsMessageSender.getSmsChannelSender(channel).signList();
        }
        else {
            return smsMessageSender
                    .getSmsChannelSenderList()
                    .stream()
                    .flatMap(s -> s.signList().stream())
                    .toList();
        }
    }

    @Plugin(name = "编辑签名", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[message_server_sms:get]')")
    @GetMapping("/{channel}/{id:\\d+}")
    public SmsSignResponseBody get(
            @PathVariable
            String channel,
            @RequestParam
            String id
    ) {
        return smsMessageSender.getSmsChannelSender(channel).getSign(id);
    }

    @PostMapping
    @Plugin(name = "保存签名", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[message_server_sms:save]')")
    public SmsSignResponseBody save(
            @RequestBody
            SmsSignMetadata body
    ) {
        return smsMessageSender.getSmsChannelSender(body.getChannel().getValue()).saveSign(body);
    }

    @DeleteMapping("/{channel}")
    @Plugin(name = "删除签名", operationDataTrace = true)
    @PreAuthorize("hasAuthority('perms[message_server_sms:delete]')")
    public RestResult<Void> delete(
            @PathVariable
            String channel,
            @RequestParam
            List<String> ids
    ) {
        SmsChannelSender smsChannelSender = smsMessageSender.getSmsChannelSender(channel);
        smsChannelSender.deleteSign(ids);
        return RestResult.of("删除 [" + smsChannelSender.getType().getName() + "] 的 " + ids.size() + " 条签名成功");
    }
}
