package io.github.loncra.basic.service.message.server.resolver.support.chat;

import com.corundumstudio.socketio.SocketIOClient;
import io.github.loncra.basic.service.message.server.domain.body.chat.UserChatMessageResponseBody;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageReadEntity;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.AbstractCustomMessageMetadata;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.MessageContentMentionMetadata;
import io.github.loncra.basic.service.message.server.resolver.ChatMessageContentResolver;
import io.github.loncra.basic.service.message.server.service.chat.UserChatConversationService;
import io.github.loncra.basic.service.message.server.service.chat.UserChatManager;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.socketio.api.ReturnValueSocketResult;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import io.github.loncra.framework.socketio.core.SocketServerManager;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MentionChatMessageResolver implements ChatMessageContentResolver {

    public static final String MENTION_PREFIX = "@";

    public static final String CHAT_MESSAGE_MENTION_EVENT_NAME = "chat_message_mention";
    public static final String MENTION_EVERYONE_ID = "EVERYONE";

    private final UserChatConversationService userChatConversationService;

    private List<InstructionMessageMetadata> getAtInstructionMessageMetadata(UserChatMessageEntity entity) {
        return entity.getContent()
                .stream()
                .filter(s -> Objects.nonNull(s.get(TypeIdNameMetadata.TYPE_FIELD_NAME)))
                .filter(s -> AbstractCustomMessageMetadata.DEFAULT_TYPE_VALUE.equals(s.getOrDefault(TypeIdNameMetadata.TYPE_FIELD_NAME, StringUtils.EMPTY)))
                .filter(s -> InstructionMessageMetadata.DEFAULT_SLOT_KIND.equals(s.getOrDefault(AbstractCustomMessageMetadata.SLOT_KIND_KEY, StringUtils.EMPTY)))
                .map(s -> CastUtils.convertValue(s, InstructionMessageMetadata.class))
                .filter(s -> MENTION_PREFIX.equals(s.getPrefix()))
                .toList();
    }

    @Override
    public boolean isSupport(UserChatMessageEntity entity) {
        return CollectionUtils.isNotEmpty(getAtInstructionMessageMetadata(entity));
    }

    @Override
    public void postSend(
            ReturnValueSocketResult<UserChatMessageEntity> socketResult,
            UserChatMessageResponseBody responseBody,
            List<UserChatMessageReadEntity> readableList,
            SocketServerManager socketServerManager
    ) {
        List<InstructionMessageMetadata> metadata = getAtInstructionMessageMetadata(responseBody);
        if (metadata.stream().anyMatch(s -> MENTION_EVERYONE_ID.equals(s.getValue().getId()))) {
            metadata = metadata.stream().filter(s -> MENTION_EVERYONE_ID.equals(s.getValue().getId())).toList();
        }
        for (InstructionMessageMetadata mention : metadata) {
            List<SocketIOClient> clients;
            if (mention.getValue().getId().equals(MENTION_EVERYONE_ID)) {
                List<UserChatConversationEntity> conversations = userChatConversationService.findEnabledByRoom(responseBody.getUserChatRoomId());
                clients = conversations.stream()
                        .filter(s -> !s.getPrincipal().equals(responseBody.getPrincipal()))
                        .peek(s -> setConversationMentionThenUpdate(responseBody, s))
                        .flatMap(s -> socketServerManager.getPrincipalClients(s.getPrincipal()).stream())
                        .toList();
            } else {
                updateConversation(mention, responseBody);
                clients = socketServerManager.getPrincipalClients(mention.getValue().getId());
            }
            clients.stream()
                    .map(s -> UnicastMessageMetadata.of(s.getSessionId().toString(), CHAT_MESSAGE_MENTION_EVENT_NAME, responseBody))
                    .forEach(s -> socketResult.getMessages().add(s));
            clients.stream()
                    .map(s -> UnicastMessageMetadata.of(s.getSessionId().toString(), UserChatManager.CONVERSATION_REFRESH_BY_ROOM_ID_EVENT_NAME, responseBody.getUserChatRoomId()))
                    .forEach(s -> socketResult.getMessages().add(s));
        }
    }

    private void updateConversation(
            InstructionMessageMetadata metadata,
            UserChatMessageResponseBody responseBody
    ) {
        UserChatConversationEntity conversation = userChatConversationService.getByPrincipal(metadata.getValue().getId(), responseBody.getUserChatRoomId());
        if (Objects.isNull(conversation)) {
            return ;
        }
        setConversationMentionThenUpdate(responseBody, conversation);
    }

    private void setConversationMentionThenUpdate(
            UserChatMessageResponseBody responseBody,
            UserChatConversationEntity conversation
    ) {
        if (CollectionUtils.isEmpty(conversation.getMentions())) {
            conversation.setMentions(new LinkedList<>());
        }
        conversation.getMentions().add(MessageContentMentionMetadata.of(responseBody.getId(), responseBody.getCreationTime(), responseBody.getParticipant()));
        userChatConversationService.updateById(conversation);
    }
}
