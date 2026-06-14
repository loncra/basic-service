package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.dao.chat.UserChatMessageDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public static final String CHAT_MESSAGE_UNDO_EVENT_NAME = "chat_message_undo";

    @Transactional(rollbackFor = Exception.class)
    public List<AbstractSocketMessageMetadata<Object>> undo(
            List<String> chatMessageIds,
            AuditAuthenticationToken token
    ) {
        //ReturnValueSocketResult<UserChatMessageEntity> socketResult = new ReturnValueSocketResult<>();
        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();
        List<UserChatMessageEntity> messages = get(chatMessageIds);
        for (UserChatMessageEntity entity : messages) {
            PrincipalDetailsConstants.equals(entity, token, token.getName() + "不是 ID 为 [" + entity.getId() + "] 消息记录发送者，无法撤销。");
            if (entity.getUndo().toBoolean()) {
                continue;
            }

            entity.setUndo(YesOrNo.Yes);
            entity.setUndoTime(new Date());

            updateById(entity);
            result.add(BroadcastMessageMetadata.of(
                    entity.getChatRoomId().toString(),
                    CHAT_MESSAGE_UNDO_EVENT_NAME,
                    entity
            ));
        }

        return result;
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

    public Long getReadableAnchorId(Long roomId, String principal) {
        return getBaseMapper().getReadableAnchorId(roomId, principal);
    }

    public int positioningPageNumber(Long chatRoomId, Long messageId, int pageSize) {
        long newerCount = lambdaQuery().eq(UserChatMessageEntity::getChatRoomId, chatRoomId)
                .eq(UserChatMessageEntity::getUndo, YesOrNo.No.getValue())
                .gt(UserChatMessageEntity::getId, messageId)
                .count();
        return (int) (newerCount / pageSize) + 1;
    }
}
