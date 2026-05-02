package io.github.loncra.basic.service.message.server.controller;

import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsTemplateResponseBody;
import io.github.loncra.basic.service.message.server.resolver.support.SmsMessageSenderResolver;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 短信模版消息管理
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@RequestMapping("sms/template")
@Plugin(
        name = "短信模版管理",
        id = "sms_template",
        parent = "sms",
        authority = "perms[message_server_sms_template:list]",
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SmsTemplateController {

    private final SmsMessageSenderResolver smsMessageSender;

    @Plugin(name = "查看模版列表")
    @GetMapping("/{channel}")
    @PreAuthorize("hasAuthority('perms[message_server_sms_template:list]')")
    public List<SmsTemplateResponseBody> list(
            @PathVariable(required = false)
            String channel
    ) {
        if (StringUtils.isNotEmpty(channel)) {
            return smsMessageSender.getSmsChannelSender(channel).templateList();
        }
        else {
            return smsMessageSender
                    .getSmsChannelSenderList()
                    .stream()
                    .flatMap(s -> s.templateList().stream())
                    .toList();
        }
    }

    @Plugin(name = "编辑签名", operationDataTrace = true)
    @GetMapping("/{channel}/{id:\\d+}")
    @PreAuthorize("hasAuthority('perms[message_server_sms_template:get]')")
    public SmsTemplateResponseBody get(
            @PathVariable
            String channel,
            @PathVariable
            String id
    ) {
        return smsMessageSender.getSmsChannelSender(channel).getTemplate(id);
    }

}
