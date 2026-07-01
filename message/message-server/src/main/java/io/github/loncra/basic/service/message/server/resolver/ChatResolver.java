package io.github.loncra.basic.service.message.server.resolver;

import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatRoomBuisnessScenEnum;

import java.util.List;

/**
 * 聊天解析器
 *
 * @author maurice.chen
 */
public interface ChatResolver {

    /**
     * 获取聊天房间业务场景
     *
     * @return 聊天房间业务场景
     */
    UserChatRoomBuisnessScenEnum getBusinessScene();


    void postConversationCreated(
            UserChatConversationEntity entity,
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participantList
    );
}
