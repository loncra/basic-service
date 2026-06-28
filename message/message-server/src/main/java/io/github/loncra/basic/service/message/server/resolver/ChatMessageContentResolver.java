package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageReadEntity;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.core.SocketServerManager;

import java.util.List;

public interface ChatMessageContentResolver {

    boolean isSupport(UserChatMessageEntity entity);

    void postSend(
            ReturnValueSocketResult<UserChatMessageEntity> socketResult,
            UserChatMessageResponseBody responseBody,
            List<UserChatMessageReadEntity> readableList,
            SocketServerManager socketServerManager
    );
}
