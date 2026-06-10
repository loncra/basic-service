package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.config.UserChatConfig;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatConversationResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageReadResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.*;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.TextMessageMetadata;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.basic.service.message.server.enumerate.UserChatMessageTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.UserChatParticipantTypeEnum;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomBuisnessScenEnum;
import io.github.loncra.basic.service.message.server.enumerate.UserChatRoomTypeEnum;
import io.github.loncra.basic.service.message.server.resolver.ChatRoomResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.annotation.Time;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.id.number.LongIdEntity;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.SocketPrincipal;
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import io.github.loncra.framework.socketio.core.SocketServerManager;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.security.core.entity.support.MobileSecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserChatManager {

    public static final String CHAT_ROOM_JOIN_EVENT_NAME = "chat_room_join";
    public static final String CHAT_ROOM_RENAME_EVENT_NAME = "chat_room_rename";
    public static final String CHAT_ROOM_LEVEL_EVENT_NAME = "chat_room_leve";
    public static final String CHAT_MESSAGE_EVENT_NAME = "chat_message";
    public static final String CHAT_MESSAGE_READ_EVENT = "chat_message_read";
    public static final String CHAT_MESSAGE_READ_UPDATE_EVENT = "chat_message_read_update";
    public static final String CHAT_CONVERSATION_CREATE_EVENT = "chat_conversation_create";

    public final static String CONCURRENT_PREFIX = "loncra:basic-service:message:app:chat:root:concurrent:";

    private final UserChatConfig userChatConfig;

    private final List<ChatRoomResolver> chatRoomResolvers;

    private final UserChatRoomService userChatRoomService;

    private final UserChatMessageService userChatMessageService;

    private final UserChatParticipantService userChatParticipantService;

    private final UserChatMessageReadService userChatMessageReadService;

    private final UserChatConversationService userChatConversationService;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final SystemUserServiceClient systemUserServiceClient;

    private final SocketServerManager socketServerManager;

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

        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天房间信息");

        List<UserChatParticipantEntity> participants = userChatParticipantService.findByRoomId(room.getId());
        UserChatParticipantEntity currentParticipant = participants.stream()
                .filter(s -> Strings.CS.equals(s.getPrincipal(), token.getName()))
                .findFirst()
                .orElseThrow(() -> new SystemException("您不能在该会话中聊天"));

        UserChatMessageEntity entity = new UserChatMessageEntity();
        entity.setPrincipal(token.getName());
        entity.setChatRoomId(room.getId());
        entity.setContent(messages);
        entity.setType(UserChatMessageTypeEnum.USER);
        userChatMessageService.insert(entity);

        List<UserChatMessageReadEntity> readableList = participants.stream()
                .filter(s -> !Strings.CS.equals(s.getPrincipal(), token.getName()))
                .map(s -> UserChatMessageReadEntity.of(entity.getId(), s.getPrincipal()))
                .peek(userChatMessageReadService::insert)
                .toList();

        ReturnValueSocketResult<UserChatMessageEntity> socketResult = new ReturnValueSocketResult<>();

        for (UserChatMessageReadEntity readable : readableList) {
            UserChatConversationEntity targetConversation = userChatConversationService.getByPrincipal(readable.getPrincipal(), chatRoomId);
            if (Objects.nonNull(targetConversation)) {
                continue;
            }
            createConversationThenAddSocketMessage(readable.getPrincipal(), room, participants, socketResult);
        }

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

        socketResult.getMessages().add(BroadcastMessageMetadata.of(chatRoomId.toString(), CHAT_MESSAGE_EVENT_NAME, responseBody));
        socketResult.setReturnValue(responseBody);

        return socketResult;
    }

    private void createConversationThenAddSocketMessage(
            String principal,
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participants,
            SocketResult socketResult
    ) {
        UserChatConversationEntity conversation = createUserChatConversationEntity(principal, room, participants);
        UserChatConversationResponseBody body = convertUserChatConversationByRoom(room, conversation);
        TypeIdNameMetadata metadata = TypeIdNameMetadata.ofPrincipalString(principal);
        SecurityContext securityContext = socketServerManager.getAccessTokenContextRepository()
                .getSecurityContext(metadata.getType(), metadata.getId());

        List<SocketPrincipal> socketPrincipals = socketServerManager.getSocketPrincipals(securityContext);
        List<UnicastMessageMetadata<UserChatConversationResponseBody>> unicastMessages = socketPrincipals.stream()
                .map(MobileSecurityPrincipal::getDeviceIdentified)
                .map(device -> socketServerManager.getSocketServer().getClient(UUID.fromString(device)))
                .filter(Objects::nonNull)
                .peek(client -> client.joinRoom(room.getId().toString()))
                .map(client -> UnicastMessageMetadata.of(client.getSessionId().toString(), CHAT_CONVERSATION_CREATE_EVENT, body))
                .toList();
        socketResult.getMessages().addAll(unicastMessages);
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
                    .sorted(Comparator.comparing((UserChatConversationResponseBody u) -> Optional.ofNullable(u.getLastUserMessage())
                            .map(LongIdEntity::getCreationTime)
                            .orElse(u.getCreationTime())).reversed())
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

    private ReturnValueSocketResult<UserChatConversationResponseBody> doCreateConversation(
            UserChatRoomEntity userChatRoomEntity,
            AuditAuthenticationToken token,
            List<String> principals
    ) {
        UserChatRoomEntity orm = userChatRoomService.getByBusiness(userChatRoomEntity.getBusinessId(), userChatRoomEntity.getBusinessScene());
        if (Objects.nonNull(orm)) {
            UserChatConversationEntity conversation = createUserChatConversationEntity(token.getName(), orm, new LinkedList<>());
            UserChatConversationResponseBody body = convertUserChatConversationByRoom(orm, conversation);
            return new ReturnValueSocketResult<>(body);
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
        tokenParticipant.setType(UserChatParticipantTypeEnum.MEMBER);
        if (userChatRoomEntity.getType().equals(UserChatRoomTypeEnum.GROUP_CHAT)) {
            tokenParticipant.setType(UserChatParticipantTypeEnum.OWNER);
        }

        List<UserChatParticipantEntity> participantList = new LinkedList<>();
        participantList.add(tokenParticipant);
        principals.stream()
                .map(s -> createUserChatParticipantEntity(userChatRoomEntity.getId(), s))
                .forEach(participantList::add);
        participantList.forEach(userChatParticipantService::insert);

        UserChatConversationEntity conversation = createUserChatConversationEntity(token.getName(), userChatRoomEntity, participantList);
        UserChatConversationResponseBody body = convertUserChatConversationByRoom(userChatRoomEntity, conversation);
        ReturnValueSocketResult<UserChatConversationResponseBody> result = new ReturnValueSocketResult<>(body);
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(userChatRoomEntity.getType())) {
            BroadcastMessageMetadata<List<UserChatParticipantEntity>> metadata = BroadcastMessageMetadata.of(
                    userChatRoomEntity.getId().toString(),
                    CHAT_ROOM_JOIN_EVENT_NAME,
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
            String principal,
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participantList
    ) {
        UserChatConversationEntity entity = userChatConversationService.getByPrincipal(principal, room.getId());
        if (Objects.nonNull(entity)) {
            return entity;
        }

        entity = new UserChatConversationEntity();
        entity.setPinned(YesOrNo.No);
        entity.setUserChatRoomId(room.getId());
        entity.setPrincipal(principal);
        entity.setMuted(YesOrNo.No);

        if (CollectionUtils.isEmpty(participantList)) {
            participantList = userChatParticipantService.findByRoomId(room.getId());
        }

        String name = participantList.stream()
                .map(s -> CastUtils.convertValue(s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE))
                .filter(s -> !Objects.equals(principal, s.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY)))
                .map(PrincipalDetailsConstants::getPrincipalName)
                .limit(userChatConfig.getConversationNameLimit())
                .collect(Collectors.joining(CastUtils.COMMA));
        entity.setName(name);

        Stream<Map<String, Object>> stream = getConversationCoverStream(principal, room, participantList);

        List<FileObject> cover = stream
                .map(s -> s.get(PrincipalDetailsConstants.AVATAR_KEY))
                .filter(Objects::nonNull)
                .map(s -> CastUtils.convertValue(s, FileObject.class))
                .limit(userChatConfig.getConversationCoverLimit())
                .toList();
        entity.setCover(cover);
        UserChatMessageEntity lastMessage = userChatMessageService.getLastMessageByRoomId(room.getId());
        if (Objects.nonNull(lastMessage)) {
            entity.setLastUserChatMessageId(lastMessage.getId());
        }

        userChatConversationService.insert(entity);

        return entity;
    }

    private @NonNull Stream<Map<String, Object>> getConversationCoverStream(
            String principal,
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participantList
    ) {
        Stream<Map<String, Object>> stream = participantList.stream()
                .map(s -> CastUtils.convertValue(s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE))
                .filter(s -> !Objects.equals(principal, s.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY)));
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            stream = participantList.stream()
                    .map(s -> CastUtils.convertValue(s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE));
        }
        return stream;
    }

    private UserChatParticipantEntity createUserChatParticipantEntity(
            Long roomId,
            String principal
    ) {
        return createUserChatParticipantEntity(roomId, principal, UserChatParticipantTypeEnum.MEMBER);
    }

    private UserChatParticipantEntity createUserChatParticipantEntity(
            Long roomId,
            String principal,
            UserChatParticipantTypeEnum type
    ) {
        UserChatParticipantEntity tokenParticipant = new UserChatParticipantEntity();
        tokenParticipant.setType(type);
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
                .orderByDesc(IdEntity::getId);
        Page<UserChatMessageEntity> page = userChatMessageService.findPage(request, wrapper);

        List<UserChatMessageEntity> messages = page.getElements();
        if (CollectionUtils.isNotEmpty(messages)) {
            List<UserChatParticipantEntity> participants = messages.stream()
                    .map(UserChatMessageEntity::getPrincipal)
                    .distinct()
                    .map(principal -> userChatParticipantService.getByChatRoomIdAndPrincipal(chatRoomId, principal))
                    .filter(Objects::nonNull)
                    .toList();
            List<UserChatMessageResponseBody> responses = messages.stream()
                    .map(m -> convertResponseBody(m, token, participants))
                    .toList();
            page.setElements(responses.stream().sorted(Comparator.comparing(UserChatMessageResponseBody::getId)).collect(Collectors.toList()));
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
            List<UserChatMessageReadEntity> reads = userChatMessageReadService.lambdaQuery()
                    .in(UserChatMessageReadEntity::getChatMessageId, entity.getId())
                    .eq(UserChatMessageReadEntity::getPrincipal, token.getName())
                    .list()
                    .stream()
                    .filter(read -> read.getReadable().toBoolean())
                    .peek(read -> read.setReadTime(Instant.now()))
                    .peek(read -> read.setReadable(YesOrNo.No))
                    .peek(userChatMessageReadService::updateById)
                    .toList();
            UserChatMessageResponseBody body = convertResponseBody(entity, token, participants);
            BroadcastMessageMetadata<UserChatMessageResponseBody> metadata = BroadcastMessageMetadata.of(
                    entity.getChatRoomId().toString(),
                    CHAT_MESSAGE_READ_EVENT,
                    body
            );
            socketResult.getMessages().add(metadata);
            List<IdValueMetadata<Long, Instant>> readBroadcasts = reads.stream()
                    .map(s -> IdValueMetadata.of(s.getId(), s.getReadTime()))
                    .toList();
            BroadcastMessageMetadata<List<IdValueMetadata<Long, Instant>>> readBroadcastMetadata = BroadcastMessageMetadata.of(
                    entity.getChatRoomId().toString(),
                    CHAT_MESSAGE_READ_UPDATE_EVENT,
                    readBroadcasts
            );
            socketResult.getMessages().add(readBroadcastMetadata);
        }

        return socketResult;
    }

    public Map<Long, Long> countUnreadQuantity(AuditAuthenticationToken token) {
        List<UserChatConversationEntity> list = userChatConversationService.findByPrincipal(token.getName());
        List<UserChatConversationEntity> filter = list.stream()
                .filter(s -> YesOrNo.No.equals(s.getMuted()))
                .toList();
        Map<Long, Long> result = new HashMap<>();
        for (UserChatConversationEntity entity : filter) {
            Long count = userChatMessageService.countReadable(entity.getUserChatRoomId(), token.getName());
            result.put(entity.getId(), count);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "addRoomParticipant:[#chatRoomId]")
    public SocketResult addRoomParticipant(
            Long chatRoomId,
            List<String> principals,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天房间信息");

        List<UserChatParticipantEntity> participants = new LinkedList<>();
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
            SystemException.isTrue(UserChatParticipantTypeEnum.OWNER_TYPE.contains(participant.getType()), "您不是管理员，无法添加成员");
        } else {
            userChatParticipantService.findByRoomId(room.getId())
                    .forEach(participant -> principals.add(participant.getPrincipal()));
            room = new UserChatRoomEntity();
            room.setBusinessScene(UserChatRoomBuisnessScenEnum.IM);
            room.setBusinessId(DigestUtils.md5DigestAsHex((token.getName() + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)));
            room.setType(UserChatRoomTypeEnum.GROUP_CHAT);
            userChatRoomService.insert(room);
        }

        for (String principal : principals) {
            UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), principal);
            if (Objects.nonNull(participant)) {
                participants.add(participant);
                continue;
            }
            UserChatParticipantTypeEnum type = Strings.CS.equals(principal, token.getName())
                    ? UserChatParticipantTypeEnum.OWNER
                    : UserChatParticipantTypeEnum.MEMBER;

            participant = createUserChatParticipantEntity(
                    room.getId(),
                    principal,
                    type
            );
            userChatParticipantService.insert(participant);
            participants.add(participant);
        }

        UserChatConversationEntity conversation = createUserChatConversationEntity(token.getName(), room, participants);
        UserChatConversationResponseBody body = convertUserChatConversationByRoom(room, conversation);

        ReturnValueSocketResult<UserChatConversationResponseBody> socketResult = new ReturnValueSocketResult<>(body);
        for (String principal : principals.stream().filter(s -> !Strings.CS.equals(s, token.getName())).toList()) {
            createConversationThenAddSocketMessage(principal, room, participants, socketResult);
        }

        String names = participants.stream()
                .map(s -> s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY))
                .map(s -> CastUtils.convertValue(s, CastUtils.MAP_TYPE_REFERENCE))
                .map(PrincipalDetailsConstants::getPrincipalName)
                .collect(Collectors.joining(CastUtils.COMMA));

        String content = MessageFormat.format(userChatConfig.getJoinRoomText(), names);
        BroadcastMessageMetadata<String> metadata = BroadcastMessageMetadata.of(
                room.getId().toString(),
                CHAT_ROOM_JOIN_EVENT_NAME,
                content
        );

        UserChatMessageEntity message = new UserChatMessageEntity();
        message.setPrincipal(token.getName());
        message.setChatRoomId(room.getId());
        message.setContent(CastUtils.convertValue(List.of(TextMessageMetadata.of(content)), CastUtils.LIST_MAP_TYPE_REFERENCE));
        message.setType(UserChatMessageTypeEnum.SYSTEM);
        userChatMessageService.insert(message);

        socketResult.getMessages().add(metadata);

        return socketResult;
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "removeRoomParticipant:[#chatRoomId]")
    public SocketResult removeRoomParticipant(
            Long chatRoomId,
            List<String> principals,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天房间信息");

        UserChatParticipantEntity owner = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
        SystemException.isTrue(UserChatParticipantTypeEnum.OWNER_TYPE.contains(owner.getType()), "您不是管理员，无法添加成员");
        SocketResult result = new SocketResult();
        for (String principal : principals) {
            UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), principal);
            if (Objects.isNull(participant)) {
                continue;
            }
            userChatParticipantService.deleteByEntity(participant);
            UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(principal, room.getId());
            if (Objects.nonNull(conversation)) {
                userChatConversationService.lambdaUpdate()
                        .set(UserChatConversationEntity::getEnabled, YesOrNo.No.getValue())
                        .eq(IdEntity::getId, conversation.getId())
                        .update();
            }
            TypeIdNameMetadata type = TypeIdNameMetadata.ofPrincipalString(principal);
            SecurityContext securityContext = socketServerManager.getAccessTokenContextRepository()
                    .getSecurityContext(type.getType(), type.getId());
            List<SocketPrincipal> socketPrincipals = socketServerManager.getSocketPrincipals(securityContext);
            List<UnicastMessageMetadata<Long>> unicastMessageMetadata = socketPrincipals.stream()
                    .map(MobileSecurityPrincipal::getDeviceIdentified)
                    .peek(device -> socketServerManager.getSocketServer().getClient(UUID.fromString(device)).leaveRoom(room.getId().toString()))
                    .map(device -> UnicastMessageMetadata.of(device, CHAT_ROOM_LEVEL_EVENT_NAME, room.getId()))
                    .toList();
            result.getMessages().addAll(unicastMessageMetadata);
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void roomRename(
            Long chatRoomId,
            String newName,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天房间信息");

        UserChatParticipantEntity owner = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
        SystemException.isTrue(UserChatParticipantTypeEnum.OWNER_TYPE.contains(owner.getType()), "您不是管理员，无法修改名称");

        List<UserChatConversationEntity> conversations = userChatConversationService.findEnabledByRoom(room.getId());
        for  (UserChatConversationEntity c : conversations) {
            userChatConversationService.lambdaUpdate()
                    .set(UserChatConversationEntity::getName, newName)
                    .eq(IdEntity::getId, c.getId())
                    .update();
        }

        IdValueMetadata<Long,String> metadata = IdValueMetadata.of(room.getId(), newName);
        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        metadata.setMetadata(details.getMetadata());
        BroadcastMessageMetadata<IdValueMetadata<Long,String>> message = BroadcastMessageMetadata.of(
                room.getId().toString(),
                CHAT_ROOM_RENAME_EVENT_NAME,
                metadata
        );
        socketServerManager.sendMessage(message);
    }

    public List<UserChatMessageReadEntity> findMessageReader(
            Long messageId,
            AuditAuthenticationToken token
    ) {
        UserChatMessageEntity message = userChatMessageService.get(messageId);
        SystemException.isTrue(Objects.nonNull(message), "找不到 ID 为 [" +  messageId + "] 的聊天消息");

        UserChatRoomEntity room = userChatRoomService.get(message.getChatRoomId());
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  message.getChatRoomId() + "] 的聊天房间信息");

        List<UserChatParticipantEntity> participants = userChatParticipantService.findByRoomId(room.getId());
        SystemException.isTrue(participants.stream().anyMatch(s -> s.getPrincipal().equals(token.getName())), "你没有权限查看该消息内容");

        List<UserChatMessageReadEntity> result = userChatMessageReadService.getByChatMessageId(message.getId());
        return result.stream()
                .map(s -> convertUserChatMessageReadResponseBody(s, participants))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private UserChatMessageReadResponseBody convertUserChatMessageReadResponseBody(
            UserChatMessageReadEntity userChatMessageReadEntity,
            List<UserChatParticipantEntity> participants
    ) {
        UserChatMessageReadResponseBody body = CastUtils.of(userChatMessageReadEntity, UserChatMessageReadResponseBody.class);
        participants.stream()
                .filter(s -> userChatMessageReadEntity.getPrincipal().equals(s.getPrincipal()))
                .findFirst()
                .map(s -> CastUtils.of(s, UserChatParticipantMetadata.class))
                .ifPresent(body::setParticipant);
        return body;
    }
}
