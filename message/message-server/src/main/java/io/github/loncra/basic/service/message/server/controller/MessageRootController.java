package io.github.loncra.basic.service.message.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.server.resolver.MessageSenderResolver;
import io.github.loncra.basic.service.message.server.resolver.MessageTypeResolver;
import io.github.loncra.basic.service.message.server.resolver.support.AbstractMessageSenderResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息发送管理
 *
 * @author maurice
 */
@RestController
@RequiredArgsConstructor
public class MessageRootController {

    private final List<MessageSenderResolver> messageSenderResolvers;

    private final List<MessageTypeResolver> messageTypeResolvers;

    /**
     * 获取消息类型
     *
     * @param category 类别
     *
     * @return 带名称的 id 元数据集合
     */
    @GetMapping("messageTypes")
    @PreAuthorize("isAuthenticated()")
    public List<IdNameMetadata> getMessageTypes(
            @RequestParam
            String category,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        Assert.isTrue(
                AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前 Authentication 非 AuditAuthenticationToken 实例，不支获取消息类型"
        );

        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        MessageTypeResolver messageTypeResolver = messageTypeResolvers
                .stream()
                .filter(m -> m.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类目为 [" + category + "] 的解析器支持"));

        return messageTypeResolver
                .getMessageTypeList(token)
                .stream()
                .map(m -> IdNameMetadata.of(m.getValue().toString(), m.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 发送消息
     *
     * @param body http servlet request body
     *
     * @return 消息结果集
     */
    @PostMapping(value = "send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RestResult<Object> send(
            @RequestBody
            Map<String, Object> body,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) throws Exception {
        String type = body.get(MessageConstants.DEFAULT_MESSAGE_TYPE_KEY).toString();
        List<Map<String, Object>> messages = CastUtils.convertValue(
                body.get(AbstractMessageSenderResolver.DEFAULT_BATCH_MESSAGE_KEY),
                new TypeReference<>() {}
        );
        if (CollectionUtils.isNotEmpty(messages)) {
            messages.forEach(m -> m.put(SystemConstants.PRINCIPAL_FIELD_NAME, securityContext.getAuthentication().getName()));
            body.put(AbstractMessageSenderResolver.DEFAULT_BATCH_MESSAGE_KEY, messages);
        }
        else {
            body.put(SystemConstants.PRINCIPAL_FIELD_NAME, securityContext.getAuthentication().getName());
        }
        return getMessageService(type).send(body);
    }

    /**
     * 按类型分组获取站内信未读数量
     *
     * @return 按类型分组的未读数量
     */
    @GetMapping("unreadQuantity")
    @PreAuthorize("isAuthenticated()")
    public Map<Integer, Long> unreadQuantity(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<Map<Integer, Long>> result = messageSenderResolvers.stream().map(s -> s.countUnreadQuantity(token)).toList();
        return result.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));
    }

    /**
     * 更具类型获取验证码服务
     *
     * @param type 消息类型
     *
     * @return 验证码服务
     */
    private MessageSenderResolver getMessageService(String type) {
        return messageSenderResolvers
                .stream()
                .filter(c -> c.getMessageType().equals(type))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为[ " + type + " ]的消息发送服务"));
    }

}
