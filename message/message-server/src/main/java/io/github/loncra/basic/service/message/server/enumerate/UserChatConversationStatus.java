package io.github.loncra.basic.service.message.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatConversationStatus implements NameValueEnum<Integer> {

    /**
     * 启用
     */
    ENABLED(10, "启用"),

    /**
     * 退出
     */
    EXIST(20, "退出"),

    /**
     * 解散
     */
    DISBAND(30, "解散");
    ;

    private final Integer value;

    private final String name;
}
