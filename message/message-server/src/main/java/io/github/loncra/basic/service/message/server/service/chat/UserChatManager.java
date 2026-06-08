package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.TimeUnitEnum;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatConversationResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.*;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomBuisnessScenEnum;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomTypeEnum;
import io.github.loncra.basic.service.message.server.resolver.ChatRoomResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.annotation.Time;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserChatManager {

    public static final String DEFAULT_CHAT_ROOM_JOIN_EVENT_NAME = "chat_room_join";
    public static final String DEFAULT_CHAT_MESSAGE_EVENT_NAME = "chat_message";
    public static final String CHAT_MESSAGE_READ_EVENT = "chat_message_read";

    public final static String CONCURRENT_PREFIX = "loncra:basic-service:message:app:chat:root:concurrent:";

    private final List<ChatRoomResolver> chatRoomResolvers;

    private final UserChatRoomService userChatRoomService;

    private final UserChatMessageService userChatMessageService;

    private final UserChatParticipantService userChatParticipantService;

    private final UserChatMessageReadService userChatMessageReadService;

    private final UserChatConversationService userChatConversationService;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final SystemUserServiceClient systemUserServiceClient;

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "send:[#chatRoomId]", waitTime = @Time(5000))
    public SocketResult send(
            Long chatRoomId,
            List<Map<String, Object>> messages,
            AuditAuthenticationToken token
    ) {
        boolean role = userChatParticipantService
                .lambdaQuery()
                .eq(UserChatParticipantEntity::getChatRoomId, chatRoomId)
                .eq(UserChatParticipantEntity::getPrincipal, token.getName())
                .exists();

        SystemException.isTrue(role, token.getName() + " 用户没有权限向 id 为 [" + chatRoomId + "] 的房间发送消息");

        List<UserChatParticipantEntity> participants = userChatParticipantService.findByRoomId(chatRoomId);
        UserChatParticipantEntity currentParticipant = participants.stream()
                .filter(s -> Strings.CS.equals(s.getPrincipal(), token.getName()))
                .findFirst()
                .orElseThrow(() -> new SystemException("您不能在该会话中聊天"));

        UserChatMessageEntity entity = new UserChatMessageEntity();
        entity.setPrincipal(token.getName());
        entity.setChatRoomId(chatRoomId);
        entity.setContent(messages);
        userChatMessageService.insert(entity);

        List<UserChatMessageReadEntity> readableList = participants.stream()
                .filter(s -> Strings.CS.equals(s.getPrincipal(), token.getName()))
                .map(s -> UserChatMessageReadEntity.of(entity.getId(), s.getPrincipal()))
                .peek(userChatMessageReadService::insert)
                .toList();

        UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(token.getName(), chatRoomId);
        SystemException.isTrue(Objects.nonNull(conversation), "找不到会话记录");
        userChatConversationService.lambdaUpdate()
                .set(UserChatConversationEntity::getLastUserChatMessageId, entity.getId())
                .eq(IdEntity::getId, conversation.getId())
                .update();

        UserChatMessageResponseBody responseBody = CastUtils.of(entity, UserChatMessageResponseBody.class);
        responseBody.setParticipant(CastUtils.of(currentParticipant, UserChatParticipantMetadata.class));
        responseBody.setReadableCount(readableList.size());
        responseBody.setReadCount(readableList.size());
        responseBody.setReadable(YesOrNo.Yes);

        ReturnValueSocketResult<UserChatMessageEntity> socketResult = new ReturnValueSocketResult<>();
        socketResult.getMessages().add(BroadcastMessageMetadata.of(chatRoomId.toString(), DEFAULT_CHAT_MESSAGE_EVENT_NAME, responseBody));
        socketResult.setReturnValue(responseBody);

        return socketResult;
    }

    public List<UserChatConversationEntity> my(
            AuditAuthenticationToken token
    ) {

        List<UserChatConversationEntity> data = userChatConversationService.findByPrincipal(token.getName());
        if (CollectionUtils.isNotEmpty(data)) {
            Map<Long, UserChatRoomEntity> roomMap = new LinkedHashMap<>();
            List<UserChatConversationResponseBody> result = new LinkedList<>();
            for (UserChatConversationEntity conversation : data) {
                UserChatRoomEntity room = roomMap.computeIfAbsent(conversation.getUserChatRoomId(), userChatRoomService::get);
                result.add(convertUserChatConversationByRoom(room, conversation));
            }
            return result.stream()
                    .sorted(Comparator.comparing((UserChatConversationResponseBody u) -> u.getLastUserMessage().getCreationTime()).reversed())
                    .collect(Collectors.toList());
        }
        return data;
    }

    private UserChatConversationResponseBody convertUserChatConversationByRoom(
            UserChatRoomEntity room,
            UserChatConversationEntity conversation
    ) {
        UserChatConversationResponseBody body = CastUtils.of(conversation, UserChatConversationResponseBody.class);
        body.setRoom(room);
        if (Objects.nonNull(body.getLastUserChatMessageId())) {
            body.setLastUserMessage(userChatMessageService.get(body.getLastUserChatMessageId()));
        }
        if (Objects.isNull(body.getLastUserMessage())) {
            UserChatMessageEntity userChatMessageEntity = userChatMessageService.getLastMessageByRoomId(room.getId());
            body.setLastUserMessage(userChatMessageEntity);
            body.setLastUserChatMessageId(body.getLastUserChatMessageId());
            body.setReadableCount(userChatMessageService.countReadable(room.getId(), conversation.getPrincipal()));
            userChatConversationService.updateById(body);
        }
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public SocketResult createConversation(
            UserChatRoomEntity userChatRoomEntity,
            AuditAuthenticationToken token,
            List<String> principals
    ) {
        if (Objects.isNull(userChatRoomEntity.getBusinessScene())) {
            userChatRoomEntity.setBusinessScene(UserChatRoomBuisnessScenEnum.IM);
            List<String> sortValues = new ArrayList<>(principals);
            sortValues.add(token.getName());
            String value = sortValues.stream().sorted().collect(Collectors.joining(CastUtils.COMMA));
            userChatRoomEntity.setBusinessId(DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8)));
        }

        return concurrentInterceptor.invoke(
                CONCURRENT_PREFIX + "create-conversation:" + userChatRoomEntity.getBusinessScene().getValue() + ":" + userChatRoomEntity.getBusinessId(),
                () -> doCreateConversation(userChatRoomEntity, token, principals)
        );
    }

    private ReturnValueSocketResult<UserChatConversationEntity> doCreateConversation(
            UserChatRoomEntity userChatRoomEntity,
            AuditAuthenticationToken token,
            List<String> principals
    ) {
        UserChatRoomEntity orm = userChatRoomService.getByBusiness(userChatRoomEntity.getBusinessId(), userChatRoomEntity.getBusinessScene());
        if (Objects.nonNull(orm)) {
            UserChatConversationEntity conversation = createUserChatConversationEntity(token, orm, new LinkedList<>());
            return new ReturnValueSocketResult<>(conversation);
        }

        userChatRoomEntity.setType(UserChatRoomTypeEnum.PRIVATE_CHAT);
        if (principals.size() > BigDecimal.ONE.intValue()) {
            userChatRoomEntity.setType(UserChatRoomTypeEnum.GROUP_CHAT);
        }
        userChatRoomService.insert(userChatRoomEntity);

        UserChatParticipantEntity tokenParticipant = createUserChatParticipantEntity(
                userChatRoomEntity.getId(),
                token.getName()
        );
        if (userChatRoomEntity.getType().equals(UserChatRoomTypeEnum.GROUP_CHAT)) {
            tokenParticipant.setOwner(YesOrNo.Yes);
        }

        List<UserChatParticipantEntity> participantList = new LinkedList<>();
        participantList.add(tokenParticipant);
        principals.stream()
                .map(s -> createUserChatParticipantEntity(userChatRoomEntity.getId(), s))
                .forEach(participantList::add);
        participantList.forEach(userChatParticipantService::insert);

        UserChatConversationEntity conversation = createUserChatConversationEntity(token, userChatRoomEntity, participantList);
        ReturnValueSocketResult<UserChatConversationEntity> result = new ReturnValueSocketResult<>(conversation);
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(userChatRoomEntity.getType())) {
            BroadcastMessageMetadata<List<UserChatParticipantEntity>> metadata = BroadcastMessageMetadata.of(
                    userChatRoomEntity.getId().toString(),
                    DEFAULT_CHAT_ROOM_JOIN_EVENT_NAME,
                    participantList
            );
            result.getMessages().add(metadata);
        }
        chatRoomResolvers.stream()
                .filter(s -> s.getBusinessScene().equals(userChatRoomEntity.getBusinessScene()))
                .forEach(s -> s.postCreate(result, participantList, token));

        return result;
    }

    private UserChatConversationEntity createUserChatConversationEntity(
            AuditAuthenticationToken token,
            UserChatRoomEntity userChatRoomEntity,
            List<UserChatParticipantEntity> participantList
    ) {
        UserChatConversationEntity entity = userChatConversationService.getByPrincipal(token.getName(), userChatRoomEntity.getId());
        if (Objects.nonNull(entity)) {
            return entity;
        }

        entity = new UserChatConversationEntity();
        entity.setPinned(YesOrNo.No);
        entity.setUserChatRoomId(userChatRoomEntity.getId());
        entity.setPrincipal(token.getName());
        entity.setMuted(YesOrNo.No);

        if (CollectionUtils.isEmpty(participantList)) {
            participantList = userChatParticipantService.findByRoomId(userChatRoomEntity.getId());
        }

        String name = participantList.stream()
                .map(s -> CastUtils.convertValue(s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE))
                .filter(s -> !Objects.equals(token.getName(), s.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY)))
                .map(PrincipalDetailsConstants::getPrincipalName)
                .collect(Collectors.joining(CastUtils.COMMA));
        entity.setName(name);

        List<FileObject> cover = participantList.stream()
                .map(s -> CastUtils.convertValue(s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE))
                .filter(s -> !Objects.equals(token.getName(), s.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY)))
                .map(s -> s.get(PrincipalDetailsConstants.AVATAR_KEY))
                .filter(Objects::nonNull)
                .map(s -> CastUtils.convertValue(s, FileObject.class))
                .toList();
        entity.setCover(cover);
        UserChatMessageEntity lastMessage = userChatMessageService.getLastMessageByRoomId(userChatRoomEntity.getId());
        if (Objects.nonNull(lastMessage)) {
            entity.setLastUserChatMessageId(lastMessage.getId());
        }

        userChatConversationService.insert(entity);

        return entity;
    }

    private UserChatParticipantEntity createUserChatParticipantEntity(
            Long roomId,
            String principal
    ) {
        UserChatParticipantEntity tokenParticipant = new UserChatParticipantEntity();
        tokenParticipant.setOwner(YesOrNo.No);
        tokenParticipant.setPrincipal(principal);
        tokenParticipant.setChatRoomId(roomId);

        Map<String, Object> details = systemUserServiceClient.getSystemUser(principal);
        tokenParticipant.getMetadata().put(AuditAuthenticationToken.DETAILS_KEY, details);
        return tokenParticipant;
    }

    public Page<UserChatMessageEntity> histories(
            PageRequest request,
            Long chatRoomId,
            AuditAuthenticationToken token
    ) {
        Wrapper<UserChatMessageEntity> wrapper = Wrappers
                .<UserChatMessageEntity>lambdaQuery()
                .eq(UserChatMessageEntity::getChatRoomId, chatRoomId)
                .eq(UserChatMessageEntity::getRevoke, YesOrNo.No.getValue())
                .orderByAsc(IdEntity::getId);
        Page<UserChatMessageEntity> page = userChatMessageService.findPage(request, wrapper);

        List<UserChatMessageEntity> messages = page.getElements();
        if (CollectionUtils.isNotEmpty(messages)) {
            List<UserChatParticipantEntity> participants = messages.stream()
                    .map(UserChatMessageEntity::getPrincipal)
                    .distinct()
                    .map(principal -> userChatParticipantService.getByChatRoomIdAndPrincipal(chatRoomId, principal))
                    .toList();
            List<UserChatMessageResponseBody> responses = messages.stream()
                    .map(m -> convertResponseBody(m, token, participants))
                    .toList();
            page.setElements(new LinkedList<>(responses));
        }

        return page;
    }

    private UserChatMessageResponseBody convertResponseBody(
            UserChatMessageEntity entity,
            AuditAuthenticationToken token,
            List<UserChatParticipantEntity> participants
    ) {
        UserChatMessageResponseBody body = CastUtils.of(entity, UserChatMessageResponseBody.class);
        List<UserChatMessageReadEntity> readList = userChatMessageReadService
                .lambdaQuery()
                .eq(UserChatMessageReadEntity::getChatMessageId, body.getId())
                .list();
        body.setReadCount(readList.size());
        body.setReadableCount((int) readList.stream().filter(r -> YesOrNo.Yes.equals(r.getReadable())).count());

        UserChatParticipantMetadata metadata = participants.stream()
                .filter(s -> Strings.CS.equals(s.getPrincipal(), body.getPrincipal()))
                .map(s ->CastUtils.of(s, UserChatParticipantMetadata.class))
                .findFirst()
                .orElse(null);
        body.setParticipant(metadata);

        readList
                .stream()
                .filter(r -> r.getPrincipal().equals(token.getName()))
                .findFirst()
                .ifPresent(r -> body.setReadable(r.getReadable()));
        return body;
    }

    @Transactional(rollbackFor = Exception.class)
    public SocketResult readMessage(
            List<Long> messageIds,
            AuditAuthenticationToken token
    ) {
        ReturnValueSocketResult<UserChatMessageResponseBody> socketResult = new ReturnValueSocketResult<>();

        List<UserChatMessageEntity> messages = userChatMessageService.get(messageIds);
        Map<Long, List<UserChatMessageEntity>> grouping = messages.stream()
                .collect(Collectors.groupingBy(UserChatMessageEntity::getChatRoomId));
        List<UserChatParticipantEntity> participants = grouping.entrySet().stream().flatMap(e -> e.getValue().stream().map(UserChatMessageEntity::getPrincipal)
                .distinct()
                .map(principal -> userChatParticipantService.getByChatRoomIdAndPrincipal(e.getKey(), principal)))
                .toList();

        for (UserChatMessageEntity entity : messages) {
            userChatMessageReadService.lambdaQuery()
                    .in(UserChatMessageReadEntity::getChatMessageId, entity.getId())
                    .eq(UserChatMessageReadEntity::getPrincipal, token.getName())
                    .list()
                    .stream()
                    .filter(read -> read.getReadable().toBoolean())
                    .peek(read -> read.setReadTime(Instant.now()))
                    .peek(read -> read.setReadable(YesOrNo.No))
                    .forEach(userChatMessageReadService::updateById);
            UserChatMessageResponseBody body = convertResponseBody(entity, token, participants);
            BroadcastMessageMetadata<UserChatMessageResponseBody> metadata = BroadcastMessageMetadata.of(
                    entity.getChatRoomId().toString(),
                    CHAT_MESSAGE_READ_EVENT,
                    body
            );
            socketResult.getMessages().add(metadata);
        }

        return socketResult;
    }

    public Map<Long, Long> countUnreadQuantity(AuditAuthenticationToken token) {
        List<UserChatConversationEntity> list = userChatConversationService.findByPrincipal(token.getName());
        Map<Long, Long> result = new HashMap<>();
        for (UserChatConversationEntity entity : list) {
            Long count = userChatMessageService.countReadable(entity.getUserChatRoomId(), token.getName());
            result.put(entity.getId(), count);
        }
        return result;
    }
}
