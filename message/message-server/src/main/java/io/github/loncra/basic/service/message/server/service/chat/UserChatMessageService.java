package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.message.server.dao.chat.UserChatMessageDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 *
 * tb_user_chat_message 的业务逻辑
 *
 * <p>Table: tb_user_chat_message - 聊天房间消息</p>
 *
 * @author maurice.chen
 * @see UserChatMessageEntity
 * @since 2025-06-01 06:31:44
 */
@Service
@RequiredArgsConstructor
public class UserChatMessageService extends BasicService<UserChatMessageDao, UserChatMessageEntity> {

    public UserChatMessageEntity getLastMessageByRoomId(Long id) {
        Page<UserChatMessageEntity> page = findPage(
                PageRequest.of(1),
                Wrappers.<UserChatMessageEntity>lambdaQuery().eq(UserChatMessageEntity::getUserChatRoomId, id).orderByDesc(IdEntity::getId)
        );
        if (CollectionUtils.isEmpty(page.getElements())) {
            return null;
        }
        return page.getElements().getFirst();
    }

    public long countReadable(
            Long roomId,
            String principal
    ) {
        return getBaseMapper().countReadable(roomId, principal);
    }

    public Long getReadableAnchorId(
            Long roomId,
            String principal
    ) {
        return getBaseMapper().getReadableAnchorId(roomId, principal);
    }

    public int positioningPageNumber(
            Long chatRoomId,
            Long messageId,
            int pageSize
    ) {
        long newerCount = lambdaQuery().eq(UserChatMessageEntity::getUserChatRoomId, chatRoomId)
                .eq(UserChatMessageEntity::getUndo, YesOrNo.No.getValue())
                .gt(UserChatMessageEntity::getId, messageId)
                .count();
        return (int) (newerCount / pageSize) + 1;
    }
}
