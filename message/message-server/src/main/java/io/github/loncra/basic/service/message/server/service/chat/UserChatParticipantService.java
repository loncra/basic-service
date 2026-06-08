package io.github.loncra.basic.service.message.server.service.chat;

import io.github.loncra.basic.service.message.server.dao.chat.UserChatParticipantDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * tb_user_chat_participant 的业务逻辑
 *
 * <p>Table: tb_user_chat_participant - 聊天房间参与者</p>
 *
 * @see UserChatParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Service
@RequiredArgsConstructor
public class UserChatParticipantService extends BasicService<UserChatParticipantDao, UserChatParticipantEntity> {

    public List<UserChatParticipantEntity> findByPrincipal(String principal) {
        return lambdaQuery().eq(UserChatParticipantEntity::getPrincipal, principal).list();
    }

    public List<UserChatParticipantEntity> findByRoomId(Long roomId) {
        return lambdaQuery().eq(UserChatParticipantEntity::getChatRoomId, roomId).list();
    }

    public UserChatParticipantEntity getByChatRoomIdAndPrincipal(Long roomId, String principal) {
        return lambdaQuery().eq(UserChatParticipantEntity::getChatRoomId, roomId)
                .eq(UserChatParticipantEntity::getPrincipal, principal)
                .one();
    }
}
