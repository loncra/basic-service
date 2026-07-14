package io.github.loncra.basic.service.message.server.service.chat.call;

import io.github.loncra.basic.service.message.server.dao.chat.call.UserChatCallParticipantDao;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatParticipantTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallParticipantStatusEnum;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * tb_user_chat_call_participant 的业务逻辑
 *
 * <p>Table: tb_user_chat_call_participant - 聊天房间参与者</p>
 *
 * @see UserChatCallParticipantEntity
 *
 * @author maurice.chen
 *
 * @since 2026-06-30 08:37:17
 */
@Service
@RequiredArgsConstructor
public class UserChatCallParticipantService extends BasicService<UserChatCallParticipantDao, UserChatCallParticipantEntity> {

    public List<Long> getCallingChatCallIds(String principal, Long... userChatCallIds) {
        return lambdaQuery().eq(UserChatParticipantMetadata::getPrincipal, principal)
                .in(UserChatCallParticipantEntity::getStatus, UserChatCallParticipantStatusEnum.BUSY_STATUS.stream().map(UserChatCallParticipantStatusEnum::getValue).toList())
                .notIn(ArrayUtils.isNotEmpty(userChatCallIds), UserChatCallParticipantEntity::getUserChatCallId, List.of(userChatCallIds))
                .list()
                .stream()
                .map(UserChatCallParticipantEntity::getUserChatCallId)
                .toList();
    }

    public List<UserChatCallParticipantEntity> findByUserChatCallId(
            Long getUserChatCallId,
            String... ignorePrincipals
    ) {
        return lambdaQuery().eq(UserChatCallParticipantEntity::getUserChatCallId, getUserChatCallId).list();
    }

    public UserChatCallParticipantEntity getByUserChatCallIdAndPrincipal(
            Long userChatCallId,
            Object principal
    ) {
        return lambdaQuery().eq(UserChatCallParticipantEntity::getUserChatCallId, userChatCallId)
                .eq(UserChatParticipantMetadata::getPrincipal, principal)
                .one();
    }

    public List<UserChatCallParticipantEntity> updateAllStatus(
            Long userChatCallId,
            UserChatCallParticipantStatusEnum status,
            Consumer<UserChatCallParticipantEntity> preUpdate,
            String... ignorePrincipals
    ) {
        return findByUserChatCallId(userChatCallId, ignorePrincipals).stream()
                .peek(s -> s.setStatus(status))
                .peek(preUpdate)
                .peek(this::updateById)
                .toList();
    }

    public UserChatCallParticipantEntity getByUserChatCallIdAndType(
            Long userChatCallId,
            UserChatParticipantTypeEnum type
    ) {
        return lambdaQuery().eq(UserChatCallParticipantEntity::getUserChatCallId,userChatCallId)
                .eq(UserChatParticipantMetadata::getType, type.getValue())
                .one();
    }
}
