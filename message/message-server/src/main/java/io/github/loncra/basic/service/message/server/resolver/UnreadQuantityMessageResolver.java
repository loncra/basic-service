package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.server.enumerate.UnreadQuantityGroupEnum;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;

import java.util.Map;

public interface UnreadQuantityMessageResolver {

    /**
     * 获取类型
     *
     * @return 类型
     */
    String getMessageType();

    /**
     * 统计未读消息数量
     *
     * @param token 当前用户
     *
     * @return kye 为消息类型，value 为未读数量
     */
    Map<Long, Long> countUnreadQuantity(AuditAuthenticationToken token);


    UnreadQuantityGroupEnum getGroup();
}
