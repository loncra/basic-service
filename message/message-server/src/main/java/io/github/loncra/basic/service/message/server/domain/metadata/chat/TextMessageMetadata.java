package io.github.loncra.basic.service.message.server.domain.metadata.chat;

import io.github.loncra.basic.service.message.server.domain.UserChatMessageContent;
import io.github.loncra.basic.service.message.server.enumerate.UserChatMessageContentTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
public class TextMessageMetadata implements UserChatMessageContent {

    @NonNull
    String value;

    @Override
    public UserChatMessageContentTypeEnum getType() {
        return UserChatMessageContentTypeEnum.TEXT;
    }
}
