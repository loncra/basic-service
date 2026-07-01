package io.github.loncra.basic.service.message.server.resolver.support;

import io.github.loncra.basic.service.message.api.domian.metadata.MessageConstants;
import io.github.loncra.basic.service.message.server.enumerate.chat.UnreadQuantityGroupEnum;
import io.github.loncra.basic.service.message.server.resolver.UnreadQuantityMessageResolver;
import io.github.loncra.basic.service.message.server.service.chat.UserChatManager;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatMessageSenderResolver implements UnreadQuantityMessageResolver {

    private final UserChatManager userChatManager;

    @Override
    public String getMessageType() {
        return MessageConstants.DEFAULT_CHAT_TYPE_VALUE;
    }

    @Override
    public Map<Long, Object> countUnreadQuantity(AuditAuthenticationToken token) {
        return userChatManager.countUnreadQuantity(token);
    }

    @Override
    public UnreadQuantityGroupEnum getGroup() {
        return UnreadQuantityGroupEnum.USER_CHAT;
    }
}
