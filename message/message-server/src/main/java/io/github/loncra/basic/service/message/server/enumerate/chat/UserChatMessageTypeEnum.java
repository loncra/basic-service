package io.github.loncra.basic.service.message.server.enumerate.chat;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatMessageTypeEnum implements NameValueEnum<Integer> {

    USER("用户消息",10),

    SYSTEM("系统消息",20),
    ;

    private final String name;

    private final Integer value;
}
