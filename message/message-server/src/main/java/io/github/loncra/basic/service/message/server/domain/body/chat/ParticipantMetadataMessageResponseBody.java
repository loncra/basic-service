package io.github.loncra.basic.service.message.server.domain.body.chat;

import io.github.loncra.basic.service.message.server.domain.entity.chat.UserChatMessageEntity;
import io.github.loncra.basic.service.message.server.domain.metadata.chat.UserChatParticipantMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParticipantMetadataMessageResponseBody extends UserChatMessageEntity {

    private UserChatParticipantMetadata participant;

}
