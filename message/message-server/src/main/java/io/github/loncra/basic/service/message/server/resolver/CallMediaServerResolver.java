package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatCallResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;

import java.util.List;

public interface CallMediaServerResolver {

    String getType();

    void create(
            UserChatCallEntity call,
            UserChatCallParticipantEntity caller,
            List<UserChatCallParticipantEntity> callParticipants
    );

    void completed(UserChatCallResponseBody body);

    void accept(
            UserChatCallEntity call,
            UserChatCallParticipantEntity callee
    );

    void privateSceneRejected(
            UserChatCallEntity call,
            UserChatCallParticipantEntity caller,
            UserChatCallParticipantEntity callee
    );
}
