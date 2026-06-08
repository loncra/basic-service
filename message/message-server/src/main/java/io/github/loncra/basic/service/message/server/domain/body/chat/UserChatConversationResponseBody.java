package io.github.loncra.basic.service.message.server.domain.body.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatConversationEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatRoomEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"lastUserChatMessageId", "userChatRoomId", "principal"})
public class UserChatConversationResponseBody extends UserChatConversationEntity {

    private UserChatMessageEntity lastUserMessage;

    private UserChatRoomEntity room;

    private long readableCount;
}
