package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallParticipantStatusEnum;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CallMessageMetadata extends AbstractCustomMessageMetadata {

    public static final String DEFAULT_SLOT_KIND = "call";

    private Long userChatCallId;

    private UserChatCallTypeEnum value;

    private UserChatCallParticipantStatusEnum status;

    @Override
    public String getSlotKind() {
        return DEFAULT_SLOT_KIND;
    }
}
