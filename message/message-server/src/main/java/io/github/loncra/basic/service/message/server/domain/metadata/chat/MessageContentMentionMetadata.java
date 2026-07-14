package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class MessageContentMentionMetadata implements Serializable {

    private Long messageId;

    private Instant creationTime;

    private UserChatParticipantMetadata participant;
}
