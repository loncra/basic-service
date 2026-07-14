package io.github.loncra.basic.service.message.server.enumerate.chat;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UnreadQuantityGroupEnum implements NameValueEnum<String> {

    SITE("站内信","site"),
    USER_CHAT("即时聊天","userChat"),
    ;

    private final String name;

    private final String value;

}
