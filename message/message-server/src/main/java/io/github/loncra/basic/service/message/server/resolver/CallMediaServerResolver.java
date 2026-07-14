package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatCallResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;

import java.util.List;

public interface CallMediaServerResolver {

    String getType();

    default void create(
            UserChatCallEntity call,
            UserChatCallParticipantEntity caller,
            List<UserChatCallParticipantEntity> callParticipants
    ) {

    }

    default void completed(UserChatCallResponseBody body) {

    }

    default void accept(
            UserChatCallEntity call,
            UserChatCallParticipantEntity callee
    ) {

    }

    default void rejected(
            UserChatCallResponseBody body
    ) {


    }
}
