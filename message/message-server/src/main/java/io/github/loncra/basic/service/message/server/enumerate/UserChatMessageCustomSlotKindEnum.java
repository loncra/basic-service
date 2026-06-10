package io.github.loncra.basic.service.message.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatMessageCustomSlotKindEnum implements NameValueEnum<String> {

    FILE("文件消息","file"),

    ;

    private final String name;

    private final String value;
}
