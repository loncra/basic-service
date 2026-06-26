package io.github.loncra.basic.service.message.server.service.chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.corundumstudio.socketio.SocketIOClient;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.message.server.config.UserChatConfig;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatConversationResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageReadResponseBody;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.*;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.TextMessageMetadata;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import io.github.loncra.basic.service.message.server.enumerate.*;
import io.github.loncra.basic.service.message.server.resolver.ChatResolver;
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
import io.github.loncra.framework.socketio.api.SocketResult;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import io.github.loncra.framework.socketio.core.SocketServerManager;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import io.github.loncra.framework.spring.security.core.entity.support.MobileSecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserChatManager {


    public static final String CONVERSATION_REFRESH_EVENT_NAME = "chat_conversation_refresh";
    public static final String CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME = "chat_conversation_refresh_by_room_id";
    public static final String PARTICIPANT_REFRESH_BY_ROOM_ID_EVENT_NAME = "chat_participant_refresh_by_room_id";

    public static final String CHAT_MESSAGE_EVENT_NAME = "chat_message";
    public static final String CHAT_MESSAGE_READ_EVENT_NAME = "chat_message_read";
    public static final String CHAT_MESSAGE_READ_UPDATE_EVENT = "chat_message_read_update";

    public static final String CHAT_CONVERSATION_CREATE_EVENT_NAME = "chat_conversation_create";

    public final static String CONCURRENT_PREFIX = "loncra:basic-service:message:app:chat:root:concurrent:";

    private final UserChatConfig userChatConfig;

    private final List<ChatResolver> chatResolvers;

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

        SystemException.isTrue(role, token.getName() + " 用户没有权限向 id 为 [" + chatRoomId + "] 的会话发送消息");

        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");

        List<UserChatParticipantEntity> participants = userChatParticipantService.findByRoomId(room.getId());
        UserChatParticipantEntity currentParticipant = participants.stream()
                .filter(s -> Strings.CS.equals(s.getPrincipal(), token.getName()))
                .findFirst()
                .orElseThrow(() -> new SystemException("您不能在该会话中聊天"));
        UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(token.getName(), chatRoomId);
        SystemException.isTrue(Objects.nonNull(conversation), "找不到会话记录");
        SystemException.isTrue(UserChatConversationStatus.ENABLED.equals(conversation.getStatus()), "会话状态不正确，状态应为 [" + UserChatConversationStatus.ENABLED.getName() + "], 但该状态为 [" + conversation.getStatus().getName() + "]");

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

        UserChatConversationEntity conversation = saveUserChatConversationEntity(principal, room, participants);
        UserChatConversationResponseBody body = convertUserChatConversationByRoom(room, conversation);
        List<UnicastMessageMetadata<Object>> messages = createUnicastMessageMetadata(
                principal,
                CHAT_CONVERSATION_CREATE_EVENT_NAME,
                body,
                c -> c.joinRoom(room.getId().toString())
        );
        socketResult.getMessages().addAll(messages);
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
    public UserChatConversationEntity createConversation(
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

    @Transactional(rollbackFor = Exception.class)
    public UserChatConversationEntity doCreateConversation(
            UserChatRoomEntity room,
            AuditAuthenticationToken token,
            List<String> principals
    ) {
        UserChatConversationEntity conversation;
        UserChatRoomEntity orm = userChatRoomService.getByBusiness(room.getBusinessId(), room.getBusinessScene());
        if (Objects.isNull(orm)) {
            room.setType(UserChatRoomTypeEnum.PRIVATE_CHAT);
            if (principals.size() > BigDecimal.ONE.intValue()) {
                room.setType(UserChatRoomTypeEnum.GROUP_CHAT);
            }
            room.setDeleted(YesOrNo.No);
            userChatRoomService.insert(room);
        } else {
            room = orm;
        }
        principals.add(token.getName());
        addRoomParticipant(room.getId(), principals, token);
        List<UserChatParticipantEntity> participantList = new LinkedList<>();
        for  (String principal : principals) {
            UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), principal);
            participantList.add(participant);
        }

        conversation =  saveUserChatConversationEntity(token.getName(), room, participantList);
        return convertUserChatConversationByRoom(room, conversation);
    }

    private UserChatConversationEntity saveUserChatConversationEntity(
            String principal,
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participantList
    ) {
        UserChatConversationEntity entity = userChatConversationService.getByPrincipal(principal, room.getId());
        if (Objects.isNull(entity)) {
            entity = new UserChatConversationEntity();
            entity.setPinned(YesOrNo.No);
            entity.setUserChatRoomId(room.getId());
            entity.setPrincipal(principal);
            entity.setMuted(YesOrNo.No);
        }

        entity.setStatus(UserChatConversationStatus.ENABLED);

        if (CollectionUtils.isEmpty(participantList)) {
            participantList = userChatParticipantService.findByRoomId(room.getId());
        }

        List<FileObject> cover = getParticipantDetailsStream(principal, room, participantList)
                .map(s -> s.get(PrincipalDetailsConstants.AVATAR_KEY))
                .filter(Objects::nonNull)
                .map(s -> CastUtils.convertValue(s, FileObject.class))
                .limit(userChatConfig.getConversationCoverLimit())
                .toList();
        entity.setCover(cover);

        String name = room.getName();

        if (StringUtils.isEmpty(name)) {
            name = getParticipantDetailsStream(principal, room, participantList)
                    .map(PrincipalDetailsConstants::getPrincipalName)
                    .collect(Collectors.joining(CastUtils.COMMA));
        }
        entity.setName(name);

        UserChatMessageEntity lastMessage = userChatMessageService.getLastMessageByRoomId(room.getId());
        if (Objects.nonNull(lastMessage)) {
            entity.setLastUserChatMessageId(lastMessage.getId());
        }

        userChatConversationService.save(entity);

        if (Objects.isNull(entity.getId())) {
            List<ChatResolver> resolvers = chatResolvers.stream()
                    .filter(s -> s.getBusinessScene().equals(room.getBusinessScene()))
                    .toList();
            for (ChatResolver resolver : resolvers) {
                resolver.postConversationCreated(entity, room, participantList);
            }
        }

        return entity;
    }

    private @NonNull Stream<Map<String, Object>> getParticipantDetailsStream(
            String principal,
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participantList
    ) {
        Stream<Map<String, Object>> stream = participantList.stream()
                .sorted(Comparator.comparing(UserChatParticipantEntity::getId).reversed())
                .map(s -> s.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY))
                .filter(Objects::nonNull)
                .map(s -> CastUtils.convertValue(s, CastUtils.MAP_TYPE_REFERENCE));
        if (UserChatRoomTypeEnum.PRIVATE_CHAT.equals(room.getType()) && StringUtils.isNotEmpty(principal)) {
            stream = stream.filter(s -> !Objects.equals(principal, s.get(PrincipalDetailsConstants.SYSTEM_NAME_KEY)));
        }

        return stream;
    }

    private UserChatParticipantEntity createUserChatParticipantEntity(
            String principal
    ) {
        UserChatParticipantEntity tokenParticipant = new UserChatParticipantEntity();
        tokenParticipant.setType(UserChatParticipantTypeEnum.MEMBER);
        tokenParticipant.setPrincipal(principal);

        Map<String, Object> details = systemUserServiceClient.getSystemUser(principal);
        tokenParticipant.getMetadata().put(AuditAuthenticationToken.DETAILS_KEY, details);
        return tokenParticipant;
    }

    public Page<UserChatMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            Long chatRoomId,
            boolean withoutReadableAnchor,
            boolean totalPage,
            AuditAuthenticationToken token
    ) {
        QueryWrapper<UserChatMessageEntity> wrapper = userChatMessageService.getQueryGenerator().createQueryWrapperFromMap(filter);
        wrapper.eq(UserChatMessageEntity.ROOM_ID_TABLE_FIELD_NAME, chatRoomId)
                .eq(UserChatMessageEntity.UNDO_TABLE_FIELD_NAME, YesOrNo.No.getValue())
                .orderByDesc(IdEntity.ID_FIELD_NAME);

        Page<UserChatMessageEntity> page;
        if (totalPage) {
            page = userChatMessageService.findTotalPage(request, wrapper);
        } else {
            page = userChatMessageService.findPage(request, wrapper);
        }

        List<UserChatMessageEntity> messages = page.getElements();
        if (CollectionUtils.isEmpty(messages)) {
            return page;
        }

        List<UserChatParticipantEntity> participants = messages.stream()
                .map(UserChatMessageEntity::getPrincipal)
                .distinct()
                .map(principal -> userChatParticipantService.getByChatRoomIdAndPrincipal(chatRoomId, principal))
                .filter(Objects::nonNull)
                .toList();
        List<UserChatMessageResponseBody> responses = messages.stream()
                .map(m -> convertResponseBody(m, token, participants))
                .toList();
        page.setElements(new LinkedList<>(responses));

        if (!withoutReadableAnchor) {
            Map<String, Object> metadata = setReadableAnchorMetadata(chatRoomId, token.getName(), page.getSize());
            page.setMetadata(metadata);
        }

        return page;
    }

    private Map<String, Object> setReadableAnchorMetadata(Long chatRoomId, String principal, int pageSize) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        Long readableAnchorId = userChatMessageService.getReadableAnchorId(chatRoomId, principal);
        if (Objects.isNull(readableAnchorId)) {
           return metadata;
        }
        int anchorPage = userChatMessageService.positioningPageNumber(chatRoomId, readableAnchorId, pageSize);
        if (anchorPage == 1) {
            return metadata;
        }
        metadata.put(UserChatMessageEntity.READABLE_ANCHOR_ID_KEY, readableAnchorId);
        metadata.put(UserChatMessageEntity.READABLE_ANCHOR_PAGE_KEY, anchorPage);
        return metadata;
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
                .map(s -> CastUtils.of(s, UserChatParticipantMetadata.class))
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
                    CHAT_MESSAGE_READ_EVENT_NAME,
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
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");

        List<UserChatParticipantEntity> participants = new LinkedList<>();
        for (String principal : principals) {
            UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), principal);
            if (Objects.nonNull(participant)) {
                continue;
            }

            participant = createUserChatParticipantEntity(principal);
            participants.add(participant);
        }

        participants.addAll(userChatParticipantService.findByRoomId(room.getId()));

        if (UserChatRoomTypeEnum.PRIVATE_CHAT.equals(room.getType()) && participants.size() > BigDecimal.ONE.add(BigDecimal.ONE).intValue()) {
            room = new UserChatRoomEntity();
            room.setBusinessScene(UserChatRoomBuisnessScenEnum.IM);
            room.setBusinessId(DigestUtils.md5DigestAsHex((token.getName() + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)));
            room.setType(UserChatRoomTypeEnum.GROUP_CHAT);
            room.setName(getParticipantNames(participants));
            room.setDeleted(YesOrNo.No);
            userChatRoomService.insert(room);
            participants.stream()
                    .peek(p -> p.setId(null))
                    .peek(p -> p.setVersion(1))
                    .peek(p -> p.setCreationTime(Instant.now()))
                    .filter(s -> s.getPrincipal().equals(token.getName()))
                    .findFirst()
                    .ifPresent(participant -> participant.setType(UserChatParticipantTypeEnum.OWNER));
        }

        Long finalRoomId = room.getId();
        participants.stream()
                .filter(s -> Objects.isNull(s.getId()))
                .peek(s -> s.setChatRoomId(finalRoomId))
                .peek(userChatParticipantService::insert)
                .flatMap(s -> getPrincipalClients(s.getPrincipal()).stream())
                .forEach(c -> c.joinRoom(finalRoomId.toString()));

        ReturnValueSocketResult<UserChatConversationEntity> result = new ReturnValueSocketResult<>();
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            String names = getParticipantNames(participants);
            String content = MessageFormat.format(userChatConfig.getJoinRoomText(), PrincipalDetailsConstants.getPrincipalName(token),names);
            UserChatMessageEntity message = insertSystemMessage(room.getId(), content);

            List<UserChatConversationEntity> conversations = updateUserChatConversationByRoom(room, new LinkedList<>());
            BroadcastMessageMetadata<Object> systemMessageMetadata = BroadcastMessageMetadata.of(
                    room.getId().toString(),
                    CHAT_MESSAGE_EVENT_NAME,
                    message
            );

            result.getMessages().add(systemMessageMetadata);
            Optional<UserChatConversationEntity> optional = conversations.stream()
                    .filter(s -> s.getPrincipal().equals(token.getName()))
                    .findFirst();
            if (optional.isPresent()) {
                result.setReturnValue(convertUserChatConversationByRoom(room, optional.get()));
            }
        }

        BroadcastMessageMetadata<Object> conversationRefreshMetadata = BroadcastMessageMetadata.of(
                room.getId().toString(),
                CONVERSATION_REFRESH_EVENT_NAME,
                new LinkedList<>()
        );
        result.getMessages().add(conversationRefreshMetadata);

        return result;
    }

    private List<UserChatConversationEntity> updateUserChatConversationByRoom(
            UserChatRoomEntity room,
            List<UserChatParticipantEntity> participants
    ) {
        if (CollectionUtils.isEmpty(participants)) {
            participants = userChatParticipantService.findByRoomId(room.getId());
        }
        List<UserChatConversationEntity> result = new LinkedList<>();
        for  (UserChatParticipantEntity participant : participants) {
            result.add(saveUserChatConversationEntity(participant.getPrincipal(), room, participants));
        }
        return result;
    }

    private UserChatMessageEntity insertSystemMessage(Long roomId, String content) {
        UserChatMessageEntity message = new UserChatMessageEntity();
        message.setPrincipal(UserChatMessageTypeEnum.SYSTEM.toString());
        message.setChatRoomId(roomId);
        message.setContent(CastUtils.convertValue(List.of(TextMessageMetadata.of(content)), CastUtils.LIST_MAP_TYPE_REFERENCE));
        message.setType(UserChatMessageTypeEnum.SYSTEM);
        userChatMessageService.insert(message);
        return message;
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "removeRoomParticipant:[#chatRoomId]")
    public List<AbstractSocketMessageMetadata<Object>> removeRoomParticipant(
            Long chatRoomId,
            List<String> principals,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");

        UserChatParticipantEntity owner = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
        SystemException.isTrue(UserChatParticipantTypeEnum.OWNER_TYPE.contains(owner.getType()), "您不是管理员，无法添加成员");
        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();
        List<UserChatParticipantEntity> removeRecords = new LinkedList<>();
        for (String principal : principals) {
            UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), principal);
            if (Objects.isNull(participant)) {
                continue;
            }
            userChatParticipantService.deleteByEntity(participant);
            UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(principal, room.getId());
            if (Objects.nonNull(conversation)) {
                userChatConversationService.lambdaUpdate()
                        .set(UserChatConversationEntity::getStatus, UserChatConversationStatus.REMOVE.getValue())
                        .eq(IdEntity::getId, conversation.getId())
                        .update();
            }
            removeRecords.add(participant);
            List<UnicastMessageMetadata<Object>> messages = createUnicastMessageMetadata(
                    principal,
                    CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME,
                    room.getId(),
                    c -> c.leaveRoom(room.getId().toString())
            );
            result.addAll(messages);
        }
        String notification = getParticipantNames(removeRecords);
        String text = MessageFormat.format(userChatConfig.getRoomRemoveParticipant(), PrincipalDetailsConstants.getPrincipalName(token), notification);

        UserChatMessageEntity entity = insertSystemMessage(room.getId(), text);
        result.add(BroadcastMessageMetadata.of(chatRoomId.toString(), CHAT_MESSAGE_EVENT_NAME, entity));
        updateUserChatConversationByRoom(room, new LinkedList<>());
        result.add(BroadcastMessageMetadata.of(room.getId().toString(), CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME, room.getId()));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Concurrent(value = CONCURRENT_PREFIX + "updateParticipantType:[#chatRoomId]")
    public List<AbstractSocketMessageMetadata<Object>> updateRoomParticipantType(
            Long chatRoomId,
            List<String> principals,
            UserChatParticipantTypeEnum typeValue,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");
        SystemException.isTrue(UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType()), "该会话非群聊会话，无法设置群管。");

        UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
        SystemException.isTrue(UserChatParticipantTypeEnum.OWNER.equals(participant.getType()), "您不是群主，设置群管。");

        List<UserChatParticipantEntity> updateRecords = new LinkedList<>();
        for (String principal : principals) {
            UserChatParticipantEntity entity = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), principal);
            if (Objects.isNull(entity) || typeValue.equals(entity.getType())) {
                continue;
            }
            entity.setType(typeValue);
            userChatParticipantService.updateById(entity);
            updateRecords.add(entity);
        }
        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();
        String names = getParticipantNames(updateRecords);
        String text = MessageFormat.format(
                userChatConfig.getUpdateParticipantTypeText(),
                PrincipalDetailsConstants.getPrincipalName(token),
                names,
                typeValue.getName()
        );
        UserChatMessageEntity entity = insertSystemMessage(room.getId(), text);

        result.add(BroadcastMessageMetadata.of(chatRoomId.toString(), CHAT_MESSAGE_EVENT_NAME, entity));
        updateRecords.stream()
                .flatMap(s -> createUnicastMessageMetadata(s.getPrincipal(), PARTICIPANT_REFRESH_BY_ROOM_ID_EVENT_NAME, room.getId()).stream())
                .forEach(result::add);

        return result;
    }

    private String getParticipantNames(List<UserChatParticipantEntity> participants) {
        return getParticipantNames(participants, participants.size());
    }

    private String getParticipantNames(List<UserChatParticipantEntity> participants, int limit) {
        return participants.stream()
                .map(UserChatParticipantMetadata::getMetadata)
                .map(s -> CastUtils.convertValue(s.get(AuditAuthenticationToken.DETAILS_KEY), CastUtils.MAP_TYPE_REFERENCE))
                .map(PrincipalDetailsConstants::getPrincipalName)
                .limit(limit)
                .collect(Collectors.joining(CastUtils.COMMA));
    }

    private List<UnicastMessageMetadata<Object>> createUnicastMessageMetadata(
            String principal,
            String eventName,
            Object object
    ) {
        return createUnicastMessageMetadata( principal, eventName, object, c -> {});
    }

    private List<UnicastMessageMetadata<Object>> createUnicastMessageMetadata(
            String principal,
            String eventName,
            Object object,
            Consumer<? super SocketIOClient> action
    ) {
        List<UnicastMessageMetadata<Object>> result = new LinkedList<>();
        getPrincipalClients(principal)
                .stream()
                .filter(Objects::nonNull)
                .peek(action)
                .map(client -> UnicastMessageMetadata.of(client.getSessionId().toString(), eventName, object))
                .forEach(result::add);
        return result;
    }

    private List<SocketIOClient> getPrincipalClients(String principal) {
        List<SocketIOClient> result = new LinkedList<>();
        TypeIdNameMetadata type = TypeIdNameMetadata.ofPrincipalString(principal);
        SecurityContext context = socketServerManager.getAccessTokenContextRepository().getSecurityContext(type.getType(), type.getId());
        if (Objects.isNull(context)) {
            return result;
        }
        socketServerManager.getSocketPrincipals(context)
                .stream()
                .map(MobileSecurityPrincipal::getDeviceIdentified)
                .map(device -> socketServerManager.getSocketServer().getClient(UUID.fromString(device)))
                .filter(Objects::nonNull)
                .forEach(result::add);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AbstractSocketMessageMetadata<Object>> roomRename(
            Long chatRoomId,
            String newName,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");
        if (UserChatRoomTypeEnum.GROUP_CHAT.equals(room.getType())) {
            UserChatParticipantEntity owner = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
            SystemException.isTrue(UserChatParticipantTypeEnum.OWNER_TYPE.contains(owner.getType()), "您不是管理员，无法修改名称");

            room.setName(newName);
            userChatRoomService.updateById(room);
            updateUserChatConversationByRoom(room, new LinkedList<>());

            IdValueMetadata<Long,String> metadata = IdValueMetadata.of(room.getId(), newName);
            AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
            metadata.setMetadata(details.getMetadata());
            BroadcastMessageMetadata<Object> refreshMessage = BroadcastMessageMetadata.of(
                    room.getId().toString(),
                    CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME,
                    room.getId()
            );
            String content = MessageFormat.format(
                    userChatConfig.getRoomRenameText(),
                    PrincipalDetailsConstants.getPrincipalName(token), newName
            );
            UserChatMessageEntity system = insertSystemMessage(room.getId(), content);
            BroadcastMessageMetadata<Object> systemMessage = BroadcastMessageMetadata.of(
                    room.getId().toString(),
                    CHAT_MESSAGE_EVENT_NAME,
                    system
            );
            return List.of(refreshMessage, systemMessage);
        } else {
            UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(token.getName(), room.getId());
            conversation.setName(newName);
            userChatConversationService.updateById(conversation);
            return new LinkedList<>(createUnicastMessageMetadata(token.getName(), CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME, room.getId()));
        }
    }

    public List<UserChatMessageReadEntity> findMessageReader(
            Long messageId,
            AuditAuthenticationToken token
    ) {
        UserChatMessageEntity message = userChatMessageService.get(messageId);
        SystemException.isTrue(Objects.nonNull(message), "找不到 ID 为 [" +  messageId + "] 的聊天消息");

        UserChatRoomEntity room = userChatRoomService.get(message.getChatRoomId());
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  message.getChatRoomId() + "] 的聊天会话信息");

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

    public List<UserChatParticipantEntity> findRoomParticipant(
            Long roomId,
            AuditAuthenticationToken token
    ) {
        UserChatParticipantEntity entity = userChatParticipantService.getByChatRoomIdAndPrincipal(roomId, token.getName());
        SystemException.isTrue(Objects.nonNull(entity), "您已不在聊天会话中");

        return userChatParticipantService.findByRoomId(roomId);
    }

    public UserChatConversationEntity getChatConversationByPrincipal(
            String name,
            Long chatRoomId,
            boolean convertBody
    ) {
        UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(name, chatRoomId);
        if (convertBody) {
            UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
            return convertUserChatConversationByRoom(room, conversation);
        } else {
            return conversation;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AbstractSocketMessageMetadata<Object>> participantExitRoom(
            Long chatRoomId,
            AuditAuthenticationToken token
    ) {
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");

        UserChatParticipantEntity participant = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
        SystemException.isTrue(Objects.nonNull(participant), "您非不会话的参与者，无法执行此操作");

        userChatParticipantService.deleteByEntity(participant);

        UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(token.getName(), room.getId());
        userChatConversationService.lambdaUpdate()
                .set(UserChatConversationEntity::getStatus, UserChatConversationStatus.EXIST.getValue())
                .eq(IdEntity::getId, conversation.getId())
                .update();

        List<UnicastMessageMetadata<Object>> clientEvents = createUnicastMessageMetadata(
                token.getName(),
                CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME,
                room.getId(),
                c -> c.leaveRoom(room.getId().toString())
        );

        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>(clientEvents);

        String content = MessageFormat.format(
                userChatConfig.getExistRoomText(),
                PrincipalDetailsConstants.getPrincipalName(token)
        );
        UserChatMessageEntity entity = insertSystemMessage(room.getId(), content);
        BroadcastMessageMetadata<Object> systemMessage = BroadcastMessageMetadata.of(
                room.getId().toString(),
                CHAT_MESSAGE_EVENT_NAME,
                entity
        );
        result.add(systemMessage);

        if (UserChatParticipantTypeEnum.OWNER.equals(participant.getType())) {
            UserChatParticipantEntity first = userChatParticipantService.getFirst(room.getId());
            if (Objects.nonNull(first)) {
                first.setType(UserChatParticipantTypeEnum.OWNER);
                userChatParticipantService.updateById(first);

                Map<String,Object> details = CastUtils.convertValue(
                        first.getMetadata().get(AuditAuthenticationToken.DETAILS_KEY),
                        CastUtils.MAP_TYPE_REFERENCE
                );
                String ownerContent = MessageFormat.format(
                        userChatConfig.getOwnerChangeText(),
                        PrincipalDetailsConstants.getPrincipalName(details)
                );
                UserChatMessageEntity ownerChange = insertSystemMessage(room.getId(), ownerContent);
                BroadcastMessageMetadata<Object> ownerChangeMessage = BroadcastMessageMetadata.of(
                        room.getId().toString(),
                        CHAT_MESSAGE_EVENT_NAME,
                        ownerChange
                );
                result.add(ownerChangeMessage);
            } else {
                userChatRoomService.deleteById(room);
            }
        }
        updateUserChatConversationByRoom(room, new LinkedList<>());
        BroadcastMessageMetadata<Object> refreshParticipantMessage = BroadcastMessageMetadata.of(
                room.getId().toString(),
                PARTICIPANT_REFRESH_BY_ROOM_ID_EVENT_NAME,
                room.getId()
        );
        result.add(refreshParticipantMessage);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AbstractSocketMessageMetadata<Object>> disbandRoom(
        Long chatRoomId,
        AuditAuthenticationToken token
    ){
        UserChatRoomEntity room = userChatRoomService.get(chatRoomId);
        SystemException.isTrue(Objects.nonNull(room), "找不到 ID 为 [" +  chatRoomId + "] 的聊天会话信息");

        UserChatParticipantEntity owner = userChatParticipantService.getByChatRoomIdAndPrincipal(room.getId(), token.getName());
        SystemException.isTrue(Objects.nonNull(owner), "您非会话的参与者，无法执行此操作");
        SystemException.isTrue(UserChatParticipantTypeEnum.OWNER.equals(owner.getType()), "您不是群主，无法完成此操作。");

        userChatRoomService.deleteByEntity(room);
        List<AbstractSocketMessageMetadata<Object>> result = new LinkedList<>();
        List<UserChatParticipantEntity> participants = userChatParticipantService.findByRoomId(room.getId());

        String content = MessageFormat.format(userChatConfig.getDisbandRoomText(), PrincipalDetailsConstants.getPrincipalName(token));
        UserChatMessageEntity entity = insertSystemMessage(room.getId(), content);

        for (UserChatParticipantEntity participantEntity : participants) {
            UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(participantEntity.getPrincipal(), room.getId());
            userChatConversationService.lambdaUpdate()
                    .set(UserChatConversationEntity::getStatus, UserChatConversationStatus.DISBAND.getValue())
                    .eq(IdEntity::getId, conversation.getId())
                    .update();
            List<UnicastMessageMetadata<Object>> clientConversationRefresh = createUnicastMessageMetadata(
                    participantEntity.getPrincipal(),
                    CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME,
                    room.getId(),
                    c -> c.leaveRoom(room.getId().toString())
            );
            result.addAll(clientConversationRefresh);

            List<UnicastMessageMetadata<Object>> systemMessage = createUnicastMessageMetadata(
                    participantEntity.getPrincipal(),
                    CHAT_MESSAGE_EVENT_NAME,
                    entity
            );
            result.addAll(systemMessage);
        }

        return result;
    }
}
