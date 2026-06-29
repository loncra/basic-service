package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.message.server.dao.chat.UserChatParticipantDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
        return lambdaQuery().eq(UserChatParticipantEntity::getPrincipal, principal)
                .list()
                .stream()
                .sorted(Comparator.comparing(UserChatParticipantEntity::getId).reversed())
                .toList();
    }

    public List<UserChatParticipantEntity> findByRoomId(Long roomId) {
        return lambdaQuery().eq(UserChatParticipantEntity::getUserChatRoomId, roomId)
                .list()
                .stream()
                .sorted(Comparator.comparing(UserChatParticipantEntity::getId))
                .toList();
    }

    public UserChatParticipantEntity getByChatRoomIdAndPrincipal(Long roomId, String principal) {
        return lambdaQuery().eq(UserChatParticipantEntity::getUserChatRoomId, roomId)
                .eq(UserChatParticipantEntity::getPrincipal, principal)
                .one();
    }

    public UserChatParticipantEntity getFirst(Long roomId) {
        Wrapper<UserChatParticipantEntity> wrapper = Wrappers.<UserChatParticipantEntity>lambdaQuery()
                        .eq(UserChatParticipantEntity::getUserChatRoomId, roomId);
        Page<UserChatParticipantEntity> page = findPage(PageRequest.of(1), wrapper);

        return page.getNumberOfElements() > 0 ? page.getElements().getFirst() :  null;
    }
}
