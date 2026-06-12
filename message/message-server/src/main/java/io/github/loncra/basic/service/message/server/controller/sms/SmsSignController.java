package io.github.loncra.basic.service.message.server.controller.sms;


import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.message.server.domain.body.sms.SmsSignResponseBody;
import io.github.loncra.basic.service.message.server.resolver.support.sms.SmsSignResolver;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.security.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 短信签名管理
 *
 * @author mauricee.chen
 */
@Slf4j
@RestController
@RequestMapping("sms/sign")
@Plugin(
        name = "签名管理",
        id = "sms_sign",
        parent = "sms",
        authority = "perms[message_server_sms_sign:page]",
        type = ResourceTypeEnum.NAVIGATION_DATA_TYPE,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
@RequiredArgsConstructor
public class SmsSignController {

    private final List<SmsSignResolver> resolvers;

    @PostMapping("/{channel}/page")
    @PreAuthorize("hasAuthority('perms[message_server_sms_sign:page]')")
    public Page<SmsSignResponseBody> page(
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
    public List<SmsSignResponseBody> find(
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

    @Plugin(name = "查看明细")
    @PreAuthorize("hasAuthority('perms[message_server_sms_sign:get]')")
    @GetMapping("/{channel}/{id}")
    public Object get(
            @PathVariable
            String channel,
            @PathVariable @RequestParam
            String id
    ) {
        return resolvers.stream()
                .filter(s -> s.getType().getValue().equals(channel))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + channel + "] 的渠道支持"))
                .get(id);
    }
}
