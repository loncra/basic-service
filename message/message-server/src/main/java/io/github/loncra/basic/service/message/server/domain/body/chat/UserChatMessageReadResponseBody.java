package io.github.loncra.basic.service.message.server.domain.body.chat;

import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageReadEntity;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserChatMessageReadResponseBody extends UserChatMessageReadEntity {
    private UserChatParticipantMetadata participant;
}
