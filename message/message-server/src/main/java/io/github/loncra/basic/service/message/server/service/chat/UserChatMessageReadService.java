package io.github.loncra.basic.service.message.server.service.chat;

import io.github.loncra.basic.service.message.server.dao.chat.UserChatMessageReadDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageReadEntity;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * tb_user_chat_message_read 的业务逻辑
 *
 * <p>Table: tb_user_chat_message_read - 聊天消息已读列表</p>
 *
 * @see UserChatMessageReadEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Service
@RequiredArgsConstructor
public class UserChatMessageReadService extends BasicService<UserChatMessageReadDao, UserChatMessageReadEntity> {

    public List<UserChatMessageReadEntity> findByUserChatMessageId(Long userChatMessageId) {
        return lambdaQuery().eq(UserChatMessageReadEntity::getUserChatMessageId, userChatMessageId)
                .orderByDesc(IdEntity::getId)
                .list();
    }

    public UserChatMessageReadEntity getByUserChatMessageIdAndPrincipal(
            Long userChatMessageId,
            String principal
    ) {
        return lambdaQuery().eq(UserChatMessageReadEntity::getUserChatMessageId, userChatMessageId)
                .eq(UserChatMessageReadEntity::getPrincipal, principal)
                .one();
    }
}
