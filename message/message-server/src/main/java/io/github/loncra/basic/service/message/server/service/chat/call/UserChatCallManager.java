package io.github.loncra.basic.service.message.server.service.chat.call;

import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.config.UserChatCallConfig;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatCallResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatParticipantEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.CallMessageMetadata;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatMessageTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatParticipantTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.UserChatRoomTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallParticipantStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallSceneEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallTypeEnum;
import io.github.loncra.basic.service.message.server.resolver.CallMediaServerResolver;
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
import io.github.loncra.framework.socketio.core.holder.SocketResultHolder;
import io.github.loncra.framework.socketio.core.holder.annotation.SocketMessage;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserChatCallManager {

    public static final String CHAT_CALL_EVENT_NAME = "chat_call";
    public static final String CHAT_CALL_UPDATE_EVENT_NAME = "chat_call_update";
    public static final String CHAT_CALL_COMPLETED_EVENT_NAME = "chat_call_completed";
    public static final String CHAT_CALL_CONFIRM_EVENT_NAME = "chat_call_confirm";

    public static final String CHAT_CALL_PARTICIPANT_UPDATE_EVENT_NAME = "chat_call_participant_update";

    public final static String CONCURRENT_PREFIX = "loncra:basic-service:message:app:chat:call:concurrent:";

    private final UserChatCallService userChatCallService;

    @Getter
    private final UserChatCallParticipantService userChatCallParticipantService;

    private final UserChatManager userChatManager;

    private final List<CallMediaServerResolver> callMediaServerResolvers;

    @Getter
    private final UserChatCallConfig userChatCallConfig;

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "create:[#userChatRoomId]", waitTime = @Time(5000))
    public SocketResult create(
            UserChatCallTypeEnum type,
            Long userChatRoomId,
            AuditAuthenticationToken token,
            List<String> callingPrincipals
    ) {
        SystemException.isTrue(
                CollectionUtils.isEmpty(userChatCallParticipantService.getCallingChatCallIds(token.getName())),
                "当前存在正在通话中的记录"
        );

        UserChatRoomEntity room = Objects.requireNonNull(
                userChatManager.getUserChatRoomService().get(userChatRoomId),
                "找不到 ID 为 [" + userChatRoomId + "] 的聊天房间信息"
        );

        UserChatParticipantEntity participant = Objects.requireNonNull(
                userChatManager.getUserChatParticipantService().getByChatRoomIdAndPrincipal(room.getId(), token.getName()),
                "您不是房间的参与者，无法发起通话"
        );

        UserChatCallEntity call = new UserChatCallEntity();
        call.setUserChatRoomId(userChatRoomId);
        call.setType(type);
        call.setStatus(UserChatCallStatusEnum.CONNECTING);
        call.setName(PrincipalDetailsConstants.getPrincipalName(token) + room.getType().getName() + type.getName());
        userChatCallService.insert(call);

        ReturnValueSocketResult<UserChatCallResponseBody> result = new ReturnValueSocketResult<>();

        UserChatCallParticipantEntity caller = convertCallParticipant(participant);
        caller.setUserChatCallId(call.getId());
        caller.setJoinTime(Instant.now());
        caller.setType(UserChatParticipantTypeEnum.CALLER);
        caller.setStatus(UserChatCallParticipantStatusEnum.INITIATING);

        IdValueMetadata<Long, Object> metadata = IdValueMetadata.of(
                call.getId(),
                caller.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY)
        );
        List<UserChatCallParticipantEntity> callParticipants = new ArrayList<>();
        if (UserChatRoomTypeEnum.PRIVATE_CHAT.equals(room.getType())){
            SystemException.isTrue(callingPrincipals.size() == 1,room.getType().getName() + "会话必须要有一个接听方，且不能是自己");
            SystemException.isTrue(callingPrincipals.stream().noneMatch(s -> Strings.CS.equals(s, token.getName())),"不能向自己发起通话");
            List<UserChatParticipantEntity> participants = userChatManager.getUserChatParticipantService()
                    .findByChatRoomIdAndPrincipals(room.getId(), callingPrincipals);
            SystemException.isTrue(participants.size() == 1,"房间 ID 为 [" + room.getId() + "] 的参与者数量不正确");

            UserChatCallParticipantEntity targetParticipant = convertCallParticipant(participants.getFirst());
            targetParticipant.setType(UserChatParticipantTypeEnum.CALLEE);
            targetParticipant.setUserChatCallId(call.getId());
            targetParticipant.setStatus(UserChatCallParticipantStatusEnum.RINGING);
            userChatCallParticipantService.insert(targetParticipant);
            callParticipants.add(targetParticipant);
            call.setScene(UserChatCallSceneEnum.PRIVATE);

        } else if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            caller.setType(UserChatParticipantTypeEnum.OWNER);
            List<UserChatCallParticipantEntity> participants = userChatManager.getUserChatParticipantService()
                    .findByChatRoomIdAndPrincipals(room.getId(), callingPrincipals)
                    .stream()
                    .map(this::convertCallParticipant)
                    .peek(s -> s.setType(UserChatParticipantTypeEnum.CALLEE))
                    .peek(s -> s.setUserChatCallId(call.getId()))
                    .peek(s -> s.setStatus(UserChatCallParticipantStatusEnum.RINGING))
                    .peek(userChatCallParticipantService::insert)
                    .toList();
            if (CollectionUtils.isNotEmpty(participants)) {
                call.setScene(UserChatCallSceneEnum.MEETING);
                callParticipants.addAll(participants);
            } else {
                call.setScene(UserChatCallSceneEnum.GROUP);
                BroadcastMessageMetadata<IdValueMetadata<Long, Object>> message = BroadcastMessageMetadata.of(
                        room.getId().toString(),
                        CHAT_CALL_EVENT_NAME,
                        metadata
                );
                result.getMessages().add(message);
            }
        } else {
            throw new UnsupportedOperationException(room.getType().getName() + "类型房间不支持通话房间创建");
        }

        CallMessageMetadata callMessageMetadata = new CallMessageMetadata();
        callMessageMetadata.setValue(call.getType());
        callMessageMetadata.setUserChatCallId(call.getId());
        callMessageMetadata.setCaller(token.getName());
        callMessageMetadata.setStatus(UserChatCallParticipantStatusEnum.INITIATING);
        callMessageMetadata.setScene(call.getScene());

        List<Map<String, Object>> content = CastUtils.convertValue(List.of(callMessageMetadata), CastUtils.LIST_MAP_TYPE_REFERENCE);
        ReturnValueSocketResult<UserChatMessageEntity> socketResult = userChatManager.send(userChatRoomId, content, token, UserChatMessageTypeEnum.CALL);
        result.getMessages().addAll(socketResult.getMessages());
        call.setUserChatMessageId(socketResult.getReturnValue().getId());

        userChatCallParticipantService.insert(caller);

        callMediaServerResolvers.stream()
                .filter(s -> userChatCallConfig.getMediaServer().equals(s.getType()))
                .findFirst()
                .ifPresent(s -> s.create(call, caller, callParticipants));

        userChatCallService.updateById(call);

        UserChatCallResponseBody body = convertUserChatCallEntityToResponseBody(call);
        metadata.setMetadata(CastUtils.convertValue(body, CastUtils.MAP_TYPE_REFERENCE));

        for (UserChatCallParticipantEntity callee: body.getParticipants()) {
            userChatManager.createUnicastMessageMetadata(
                            callee.getPrincipal(),
                            CHAT_CALL_EVENT_NAME,
                            metadata,
                            c -> c.joinRoom(call.getRoomId())
                    )
                    .forEach(s -> result.getMessages().add(s));
        }

        result.setReturnValue(body);
        return result;
    }

    private UserChatCallParticipantEntity convertCallParticipant(UserChatParticipantEntity participant) {
        return CastUtils.of(
                participant,
                UserChatCallParticipantEntity.class,
                IdEntity.ID_FIELD_NAME,
                VersionEntity.VERSION_FIELD_NAME,
                NumberIdEntity.CREATION_TIME_FIELD_NAME
        );
    }

    public UserChatCallResponseBody convertUserChatCallEntityToResponseBody(UserChatCallEntity userChatCallEntity) {
        if (Objects.isNull(userChatCallEntity)) {
            return null;
        }

        UserChatCallResponseBody body = CastUtils.of(userChatCallEntity, UserChatCallResponseBody.class);
        body.setRoom(userChatManager.getUserChatRoomService().get(userChatCallEntity.getUserChatRoomId()));
        body.setParticipants(userChatCallParticipantService.findByUserChatCallId(body.getId()));

        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "completed:[#userChatCallId]", waitTime = @Time(5000))
    public List<AbstractSocketMessageMetadata<Object>> completed(
            Long userChatCallId,
            String principal
    ) {

        UserChatCallEntity call = Objects.requireNonNull(userChatCallService.get(userChatCallId), "找不到 ID 为 [" + userChatCallId + "] 的通话记录");
        if (UserChatCallStatusEnum.COMPLETED.equals(call.getStatus())) {
            return List.of();
        }

        UserChatCallParticipantEntity participant = userChatCallParticipantService.getByUserChatCallIdAndPrincipal(call.getId(), principal);
        participant.setLeaveTime(Instant.now());
        if (UserChatCallParticipantStatusEnum.INITIATING.equals(participant.getStatus())) {
            participant.setStatus(UserChatCallParticipantStatusEnum.CANCELED);
        }
        else if (UserChatCallParticipantStatusEnum.ACTIVE.equals(participant.getStatus())){
            participant.setStatus(UserChatCallParticipantStatusEnum.COMPLETED);
        }
        return doCompleted(call, participant);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AbstractSocketMessageMetadata<Object>> doCompleted(
            UserChatCallEntity call,
            UserChatCallParticipantEntity participant
    ) {
        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();
        UserChatRoomEntity room = Objects.requireNonNull(userChatManager.getUserChatRoomService().get(call.getUserChatRoomId()), "找不到 ID 为 [" + call.getUserChatRoomId() + "] 的会话记录");
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            result.addAll(completedByGroupChat(participant, call));
        } else if (UserChatRoomTypeEnum.PRIVATE_CHAT.equals(room.getType())) {
            result.addAll(completedByPrivateChat(participant, call));
        } else {
            throw new UnsupportedOperationException("不支持房间类型为 [" + room.getType() + "] 的结束通话操作");
        }

        UserChatCallResponseBody body = convertUserChatCallEntityToResponseBody(call);
        callMediaServerResolvers.stream()
                .filter(s -> body.getMediaServer().equals(s.getType()))
                .findFirst()
                .ifPresent(s -> s.completed(body));

        return result;
    }

    private List<AbstractSocketMessageMetadata<Object>> completedByPrivateChat(
            UserChatCallParticipantEntity participant,
            UserChatCallEntity call
    ) {

        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();

        if (UserChatCallParticipantStatusEnum.NO_ANSWER_STATUS.contains(participant.getStatus())) {
            userChatCallParticipantService.updateAllStatus(
                    call.getId(),
                    participant.getStatus(),
                    s -> s.setLeaveTime(Instant.now()),
                    participant.getPrincipal()
            ).forEach(s -> BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_PARTICIPANT_UPDATE_EVENT_NAME, s));

            result.add(userChatManager.updateCallMessage(call.getUserChatMessageId(), participant.getStatus(), null));
        }
        else if (UserChatCallParticipantStatusEnum.COMPLETED.equals(participant.getStatus())) {

            UserChatCallParticipantStatusEnum status = participant.getType().equals(UserChatParticipantTypeEnum.CALLEE)
                    ? UserChatCallParticipantStatusEnum.COMPLETED_BY_CALLEE
                    : UserChatCallParticipantStatusEnum.COMPLETED_BY_CALLER;

            userChatCallParticipantService.updateAllStatus(
                    call.getId(),
                    status,
                    s -> s.setLeaveTime(Instant.now()),
                    participant.getPrincipal()
            ).forEach(s -> BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_PARTICIPANT_UPDATE_EVENT_NAME, s));
            result.add(userChatManager.updateCallMessage(call.getUserChatMessageId(), status, null));
        }

        userChatCallParticipantService.updateById(participant);
        result.add(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_PARTICIPANT_UPDATE_EVENT_NAME, participant));

        call.setStatus(UserChatCallStatusEnum.COMPLETED);
        call.setEndTime(Instant.now());
        userChatCallService.updateById(call);
        result.add(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_COMPLETED_EVENT_NAME, call));

        return result;
    }

    private List<AbstractSocketMessageMetadata<Object>> completedByGroupChat(
            UserChatCallParticipantEntity participant,
            UserChatCallEntity call
    ) {
        if (participant.getType().equals(UserChatParticipantTypeEnum.CALLEE)) {
            participant.setStatus(UserChatCallParticipantStatusEnum.COMPLETED_BY_GROUP_LEAVE);
            return List.of(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_PARTICIPANT_UPDATE_EVENT_NAME, participant));
        } else {
            userChatCallParticipantService.updateAllStatus(
                    call.getId(),
                    UserChatCallParticipantStatusEnum.COMPLETED_BY_CALLER,
                    s -> s.setLeaveTime(Instant.now()),
                    participant.getPrincipal()
            );

            return List.of(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_COMPLETED_EVENT_NAME, call));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "confirm:[#userChatCallId]:[#token.name]")
    public ReturnValueSocketResult<UserChatCallParticipantEntity> accept(
            Long userChatCallId,
            AuditAuthenticationToken token
    ) {

        List<Long> callingChatIds = userChatCallParticipantService.getCallingChatCallIds(token.getName(), userChatCallId);
        callingChatIds.forEach(id -> completed(id, token.getName()));

        UserChatCallEntity call = Objects.requireNonNull(
                userChatCallService.get(userChatCallId),
                "找不到 ID 为 [" + userChatCallId + "] 的通话记录"
        );

        SystemException.isTrue(UserChatCallSceneEnum.ACCEPT_SCENE.contains(call.getScene()), "通话场景非接受场景");
        SystemException.isTrue(UserChatCallStatusEnum.CONNECTING.equals(call.getStatus()), "该通过非连接中状态");

        UserChatCallParticipantEntity callee = Objects.requireNonNull(
                userChatCallParticipantService.getByUserChatCallIdAndPrincipal(call.getId(), token.getName()),
                "您不是该通话中邀请的对象"
        );
        callee.setStatus(UserChatCallParticipantStatusEnum.ACTIVE);
        callee.setJoinTime(Instant.now());

        ReturnValueSocketResult<UserChatCallParticipantEntity> result = new ReturnValueSocketResult<>(callee);

        result.getMessages().add(userChatManager.updateCallMessage(call.getUserChatMessageId(), UserChatCallParticipantStatusEnum.CONNECTING, token.getName()));
        result.getMessages().add(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_CONFIRM_EVENT_NAME, callee));

        callMediaServerResolvers.stream()
                .filter(s -> call.getMediaServer().equals(s.getType()))
                .findFirst()
                .ifPresent(s -> s.accept(call, callee));

        userChatCallParticipantService.updateById(callee);
        if (UserChatCallSceneEnum.PRIVATE.equals(call.getScene())) {
            List<UserChatCallParticipantEntity> participants = userChatCallParticipantService.findByUserChatCallId(call.getId());
            if (participants.stream().allMatch(s -> UserChatCallParticipantStatusEnum.ACTIVE.equals(s.getStatus()))) {
                call.setStatus(UserChatCallStatusEnum.ACTIVE);
                call.setStartTime(Instant.now());
                userChatCallService.updateById(call);
                result.getMessages().add(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_UPDATE_EVENT_NAME, call));
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "confirm:[#userChatCallId]:[#token.name]")
    public List<AbstractSocketMessageMetadata<Object>> rejected(
            Long userChatCallId,
            AuditAuthenticationToken token
    ) {
        UserChatCallEntity call = Objects.requireNonNull(
                userChatCallService.get(userChatCallId),
                "找不到 ID 为 [" + userChatCallId + "] 的通话记录"
        );
        SystemException.isTrue (UserChatCallSceneEnum.ACCEPT_SCENE.contains(call.getScene()), "通话场景非接受/拒绝场景");
        SystemException.isTrue(UserChatCallStatusEnum.CONNECTING.equals(call.getStatus()), "该通过非连接中状态");

        UserChatCallParticipantEntity callee = Objects.requireNonNull(
                userChatCallParticipantService.getByUserChatCallIdAndPrincipal(call.getId(), token.getName()),
                "您不是该通话中邀请的对象"
        );
        SystemException.isTrue(
                UserChatParticipantTypeEnum.CALLEE.equals(callee.getType()),
                "类型出现错误，您应该属于被叫对象，当前您的类型为 [" + callee.getType().getName() + "],无法拒绝此通话"
        );

        callee.setStatus(UserChatCallParticipantStatusEnum.REJECTED);
        callee.setLeaveTime(Instant.now());
        userChatCallParticipantService.updateById(callee);

        UserChatCallResponseBody responseBody = CastUtils.of(call, UserChatCallResponseBody.class);
        responseBody.setRoom(userChatManager.getUserChatRoomService().get(call.getUserChatRoomId()));
        responseBody.getParticipants().add(callee);

        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();
        result.add(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_CONFIRM_EVENT_NAME, callee));
        if (UserChatCallSceneEnum.PRIVATE.equals(call.getScene())) {
            call.setStatus(UserChatCallStatusEnum.COMPLETED);
            userChatCallService.updateById(call);
            UserChatCallParticipantEntity caller = userChatCallParticipantService.getByUserChatCallIdAndType(call.getId(), UserChatParticipantTypeEnum.CALLER);
            caller.setStatus(UserChatCallParticipantStatusEnum.COMPLETED);
            userChatCallParticipantService.updateById(caller);
            responseBody.getParticipants().add(caller);

            result.add(BroadcastMessageMetadata.of(call.getRoomId(), CHAT_CALL_COMPLETED_EVENT_NAME, call));
        }

        callMediaServerResolvers.stream()
                .filter(s -> call.getMediaServer().equals(s.getType()))
                .findFirst()
                .ifPresent(s -> s.rejected(responseBody));

        result.add(userChatManager.updateCallMessage(call.getUserChatMessageId(), UserChatCallParticipantStatusEnum.REJECTED, token.getName()));
        return result;
    }

    public UserChatCallEntity getUserChatCall(
            Long userChatCallId,
            AuditAuthenticationToken token
    ) {
        UserChatCallEntity call = userChatCallService.get(userChatCallId);
        UserChatConversationEntity conversation = userChatManager.getChatConversationByPrincipal(token.getName(), call.getUserChatRoomId(),false);
        SystemException.isTrue(Objects.nonNull(conversation), "您无权访问该通话");
        return call;
    }

    @SocketMessage
    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "completed:[#userChatCallId]", waitTime = @Time(5000))
    public void timeout(Long userChatCallId) {
        UserChatCallEntity call = userChatCallService.get(userChatCallId);
        if (Objects.isNull(call)) {
            return ;
        }

        if (!UserChatCallStatusEnum.CONNECTING.equals(call.getStatus())) {
            return ;
        }

        UserChatCallParticipantEntity participant = userChatCallParticipantService.getByUserChatCallIdAndType(call.getId(), UserChatParticipantTypeEnum.CALLER);
        participant.setLeaveTime(Instant.now());
        participant.setStatus(UserChatCallParticipantStatusEnum.NO_ANSWER);

        SocketResultHolder.get().getMessages().addAll(doCompleted(call, participant));
    }


}