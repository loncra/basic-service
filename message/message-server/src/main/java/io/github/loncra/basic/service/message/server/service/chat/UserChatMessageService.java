package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.dao.chat.UserChatMessageDao;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageReadEntity;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * tb_user_chat_message 的业务逻辑
 *
 * <p>Table: tb_user_chat_message - 聊天房间消息</p>
 *
 * @see UserChatMessageEntity
 *
 * @author maurice.chen
 *
 * @since 2025-06-01 06:31:44
 */
@Service
@RequiredArgsConstructor
public class UserChatMessageService extends BasicService<UserChatMessageDao, UserChatMessageEntity> {

    public static final String CHAT_MESSAGE_REVOKE_EVENT = "CHAT_MESSAGE_REVOKE";

    @Transactional(rollbackFor = Exception.class)
    public SocketResult revoke(List<String> chatMessageIds, AuditAuthenticationToken token) {
        ReturnValueSocketResult<UserChatMessageEntity> socketResult = new ReturnValueSocketResult<>();

        List<UserChatMessageEntity> messages = get(chatMessageIds);
        for (UserChatMessageEntity entity : messages) {
            PrincipalDetailsConstants.equals(entity, token, token.getName() + "不是 ID 为 [" + entity.getId() + "] 消息记录发送者，无法撤销。");
            if (entity.getRevoke().toBoolean()) {
                continue;
            }

            entity.setRevoke(YesOrNo.Yes);
            entity.setRevocationTime(new Date());

            updateById(entity);
            BroadcastMessageMetadata<UserChatMessageEntity> metadata = BroadcastMessageMetadata.of(
                    entity.getChatRoomId().toString(),
                    CHAT_MESSAGE_REVOKE_EVENT,
                    entity
            );
            socketResult.getMessages().add(metadata);
        }

        return socketResult;
    }

    public UserChatMessageEntity getLastMessageByRoomId(Long id) {
        Page<UserChatMessageEntity> page = findPage(
                PageRequest.of(1),
                Wrappers.<UserChatMessageEntity>lambdaQuery().eq(UserChatMessageEntity::getChatRoomId, id).orderByDesc(IdEntity::getId)
        );
        if (CollectionUtils.isEmpty(page.getElements())) {
            return null;
        }
        return page.getElements().getFirst();
    }

    public long countReadable(Long roomId, String principal) {
        return getBaseMapper().countReadable(roomId, principal);
    }
}
