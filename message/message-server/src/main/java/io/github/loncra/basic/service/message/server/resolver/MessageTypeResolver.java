package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.api.enumerate.MessageTypeEnum;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;

import java.util.List;

/**
 * 消息类型解析器，用于说明某类消息有什么 {@link MessageTypeEnum} 枚举值
 *
 * @author maurice.chen
 */
public interface MessageTypeResolver {

    /**
     * 获取消息类别
     *
     * @return 消息类别
     */
    String getCategory();

    /**
     * 获取消息类型枚举集合
     *
     * @param token 认证信息
     * @return 消息类型枚举集合
     */
    List<MessageTypeEnum> getMessageTypeList(AuditAuthenticationToken token);
}
