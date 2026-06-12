package io.github.loncra.basic.service.message.server.controller.sms;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsTemplateResponseBody;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsTemplateResolver;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 短信模版消息管理
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@RequestMapping("sms/template")
@Plugin(
        name = "模版管理",
        id = "sms_template",
        parent = "sms",
        authority = "perms[message_server_sms_template:page]",
        type = ResourceTypeEnum.NAVIGATION_DATA_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SmsTemplateController {

    private final List<SmsTemplateResolver> resolvers;

    @PostMapping("/{channel}/page")
    @PreAuthorize("hasAuthority('perms[message_server_sms_template:page]')")
    public Page<SmsTemplateResponseBody> page(
            PageRequest pageRequest,
            @RequestParam
            Map<String, Object> param,
            @PathVariable(required = false)
            String channel
    ) {
        return resolvers.stream()
                .filter(s -> s.getType().getValue().equals(channel))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + channel + "] 的渠道支持"))
                .page(pageRequest, param);
    }

    @PostMapping("/{channel}/find")
    @PreAuthorize("isAuthenticated()")
    public List<SmsTemplateResponseBody> find(
            @RequestParam
            Map<String, Object> param,
            @PathVariable(required = false)
            String channel
    ) {
        return resolvers.stream()
                .filter(s -> s.getType().getValue().equals(channel))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + channel + "] 的渠道支持"))
                .find(param);
    }

    @OperationDataTrace
    @Plugin(name = "查看明细")
    @GetMapping("/{channel}/{id}")
    @PreAuthorize("hasAuthority('perms[message_server_sms_template:get]')")
    public Object get(
            @PathVariable
            String channel,
            @PathVariable
            String id
    ) {
        return resolvers.stream()
                .filter(s -> s.getType().getValue().equals(channel))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + channel + "] 的渠道支持"))
                .get(id);
    }
}
