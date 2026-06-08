package io.github.loncra.basic.service.message.server.service.chat;

import io.github.loncra.basic.service.message.server.dao.chat.UserChatConversationDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * tb_user_chat_conversation 的业务逻辑
 *
 * <p>Table: tb_user_chat_conversation - 用户聊天会话记录</p>
 *
 * @see UserChatConversationEntity
 *
 * @author maurice.chen
 *
 * @since 2026-06-05 10:30:49
 */
@Service
@RequiredArgsConstructor
public class UserChatConversationService extends BasicService<UserChatConversationDao, UserChatConversationEntity> {

    public UserChatConversationEntity getByPrincipal(
            String principal,
            Long roomId
    ) {
        return lambdaQuery().eq(UserChatConversationEntity::getPrincipal, principal).eq(UserChatConversationEntity::getUserChatRoomId,  roomId).one();
    }

    public List<UserChatConversationEntity> findByPrincipal(String principal) {
        return lambdaQuery().eq(UserChatConversationEntity::getPrincipal, principal).list();
    }
}
