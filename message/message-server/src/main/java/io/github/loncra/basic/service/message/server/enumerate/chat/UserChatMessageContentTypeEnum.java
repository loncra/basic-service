package io.github.loncra.basic.service.message.server.enumerate.chat;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatMessageContentTypeEnum implements NameValueEnum<String> {

    TEXT("普通文本消息","text"),

    custom("自定义消息","custom"),
    ;

    private final String name;

    private final String value;
}
