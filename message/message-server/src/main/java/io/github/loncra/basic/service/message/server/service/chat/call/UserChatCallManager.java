package io.github.loncra.basic.service.message.server.service.chat.call;

import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatCallResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatParticipantTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatRoomTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallParticipantStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallTypeEnum;
import io.github.loncra.basic.service.message.server.service.chat.UserChatManager;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.annotation.Time;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.number.NumberIdEntity;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserChatCallManager {

    public static final String CHAT_CALL_EVENT_NAME = "chat_call";

    public static final String CHAT_CALL_COMPLETED_EVENT_NAME = "chat_call_completed";

    public static final String CHAT_CALL_LEVEL_EVENT_NAME = "chat_call_level";

    public final static String CONCURRENT_PREFIX = "loncra:basic-service:message:app:chat:call:concurrent:";

    private final UserChatCallService userChatCallService;

    private final UserChatCallParticipantService userChatCallParticipantService;

    private final UserChatManager userChatManager;

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "create:[#userChatRoomId]", waitTime = @Time(5000))
    public SocketResult create(
            UserChatCallTypeEnum type,
            Long userChatRoomId,
            AuditAuthenticationToken token,
            List<String> callingPrincipals
    ) {

        UserChatRoomEntity room = Objects.requireNonNull(
                userChatManager.getUserChatRoomService().get(userChatRoomId),
                "找不到 ID 为 [" + userChatRoomId + "] 的聊天房间信息"
        );

        UserChatParticipantEntity participant = Objects.requireNonNull(
                userChatManager.getUserChatParticipantService().getByChatRoomIdAndPrincipal(room.getId(), token.getName()),
                "您不是房间的参与者，无法发起通话"
        );

        UserChatCallEntity userChatCallEntity = new UserChatCallEntity();
        userChatCallEntity.setUserChatRoomId(userChatRoomId);
        userChatCallEntity.setType(type);
        userChatCallEntity.setStatus(UserChatCallStatusEnum.CONNECTING);
        userChatCallEntity.setName(PrincipalDetailsConstants.getPrincipalName(token) + room.getType().getName() + type.getName());
        userChatCallService.insert(userChatCallEntity);

        ReturnValueSocketResult<UserChatCallResponseBody> result = new ReturnValueSocketResult<>();

        UserChatCallParticipantEntity callParticipant = CastUtils.of(
                participant,
                UserChatCallParticipantEntity.class,
                IdEntity.ID_FIELD_NAME,
                VersionEntity.VERSION_FIELD_NAME,
                NumberIdEntity.CREATION_TIME_FIELD_NAME
        );
        callParticipant.setUserChatCallId(userChatCallEntity.getId());
        callParticipant.setJoinTime(Instant.now());
        callParticipant.setType(UserChatParticipantTypeEnum.CALLER);
        callParticipant.setStatus(UserChatCallParticipantStatusEnum.INITIATING);

        IdValueMetadata<Long, Object> metadata = IdValueMetadata.of(
                userChatCallEntity.getId(),
                callParticipant.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY)
        );

        if (UserChatRoomTypeEnum.PRIVATE_CHAT.equals(room.getType())){
            SystemException.isTrue(callingPrincipals.size() == 1,room.getType().getName() + "会话必须要有一个接听方，且不能是自己");
            SystemException.isTrue(callingPrincipals.stream().noneMatch(s -> Strings.CS.equals(s, token.getName())),"不能向自己发起通话");
            List<UserChatParticipantEntity> participants = userChatManager.getUserChatParticipantService()
                    .findByChatRoomIdAndPrincipals(room.getId(), callingPrincipals);
            SystemException.isTrue(participants.size() == 1,"房间 ID 为 [" + room.getId() + "] 的参与者数量不正确");

            UserChatCallParticipantEntity targetParticipant = CastUtils.of(
                    participants.getFirst(),
                    UserChatCallParticipantEntity.class,
                    IdEntity.ID_FIELD_NAME,
                    VersionEntity.VERSION_FIELD_NAME,
                    NumberIdEntity.CREATION_TIME_FIELD_NAME
            );
            targetParticipant.setType(UserChatParticipantTypeEnum.CALLEE);
            targetParticipant.setUserChatCallId(userChatCallEntity.getId());
            targetParticipant.setStatus(UserChatCallParticipantStatusEnum.RINGING);
            userChatCallParticipantService.insert(targetParticipant);

            userChatManager.createUnicastMessageMetadata(targetParticipant.getPrincipal(), CHAT_CALL_EVENT_NAME, metadata)
                    .forEach(s -> result.getMessages().add(s));
        } else if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            callParticipant.setType(UserChatParticipantTypeEnum.OWNER);
            BroadcastMessageMetadata.of(room.getId().toString(), CHAT_CALL_EVENT_NAME, metadata);
        }

        userChatCallParticipantService.insert(callParticipant);

        UserChatCallResponseBody body = createResponseBody(userChatCallEntity);
        result.setReturnValue(body);
        return result;
    }

    public UserChatCallResponseBody createResponseBody(UserChatCallEntity userChatCallEntity) {
        if (Objects.isNull(userChatCallEntity)) {
            return null;
        }

        UserChatCallResponseBody body = CastUtils.of(userChatCallEntity, UserChatCallResponseBody.class);
        body.setRoom(userChatManager.getUserChatRoomService().get(userChatCallEntity.getUserChatRoomId()));
        body.setParticipants(userChatCallParticipantService.findByUserChatCallId(body.getId()));

        return body;
    }

    @Concurrent(value = CONCURRENT_PREFIX + "completed:[#userChatCallId]", waitTime = @Time(5000))
    public AbstractSocketMessageMetadata<Long> completed(
            Long userChatCallId,
            AuditAuthenticationToken token
    ) {

        UserChatCallEntity call = Objects.requireNonNull(userChatCallService.get(userChatCallId), "找不到 ID 为 [" + userChatCallId + "] 的通话记录");
        SystemException.isTrue(!UserChatCallStatusEnum.COMPLETED.equals(call.getStatus()), "该通话已结束");

        UserChatCallParticipantEntity participant = userChatCallParticipantService.getByUserChatCallIdAndPrincipal(call.getId(), token.getName());
        participant.setLeaveTime(Instant.now());
        participant.setStatus(UserChatCallParticipantStatusEnum.COMPLETED);

        UserChatRoomEntity room = Objects.requireNonNull(userChatManager.getUserChatRoomService().get(call.getUserChatRoomId()), "找不到 ID 为 [" + call.getUserChatRoomId() + "] 的会话记录");
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            return completedByGroupChat(participant, call);
        } else if (UserChatRoomTypeEnum.PRIVATE_CHAT.equals(room.getType())) {
            return completedByPrivateChat(participant, call);
        } else {
            throw new UnsupportedOperationException("不支持房间类型为 [" + room.getType() + "] 的结束通话操作");
        }
    }

    private AbstractSocketMessageMetadata<Long> completedByPrivateChat(
            UserChatCallParticipantEntity participant,
            UserChatCallEntity call
    ) {

        UserChatCallParticipantStatusEnum status = participant.getType().equals(UserChatParticipantTypeEnum.CALLEE)
                ? UserChatCallParticipantStatusEnum.COMPLETED_BY_CALLEE
                : UserChatCallParticipantStatusEnum.COMPLETED_BY_CALLER;

        userChatCallParticipantService.updateAllStatus(
                call.getId(),
                status,
                s -> s.setLeaveTime(Instant.now()),
                participant.getPrincipal()
        );

        userChatCallParticipantService.updateById(participant);

        call.setStatus(UserChatCallStatusEnum.COMPLETED);
        call.setEndTime(Instant.now());
        userChatCallService.updateById(call);

        return BroadcastMessageMetadata.of(call.getUserChatRoomId().toString(), CHAT_CALL_COMPLETED_EVENT_NAME, call.getId());
    }

    private AbstractSocketMessageMetadata<Long> completedByGroupChat(
            UserChatCallParticipantEntity participant,
            UserChatCallEntity call
    ) {
        if (participant.getType().equals(UserChatParticipantTypeEnum.CALLEE)) {
            participant.setStatus(UserChatCallParticipantStatusEnum.COMPLETED_BY_GROUP_LEAVE);
            return BroadcastMessageMetadata.of(call.getUserChatRoomId().toString(), CHAT_CALL_LEVEL_EVENT_NAME, call.getId());
        } else {
            userChatCallParticipantService.updateAllStatus(
                    call.getId(),
                    UserChatCallParticipantStatusEnum.COMPLETED_BY_CALLER,
                    s -> s.setLeaveTime(Instant.now()),
                    participant.getPrincipal()
            );

            return BroadcastMessageMetadata.of(call.getUserChatRoomId().toString(), CHAT_CALL_COMPLETED_EVENT_NAME, call.getId());
        }
    }
}
