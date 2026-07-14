package io.github.loncra.basic.service.message.server.enumerate.chat;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatParticipantTypeEnum implements NameValueEnum<Integer> {

    /**
     * 房主
     */
    OWNER(10, "群主"),

    /**
     * 次房主 (管理员)
     */
    CO_OWNER(20, "管理员"),

    /**
     * 普通成员
     */
    MEMBER(30, "成员"),

    /**
     * 主叫
     */
    CALLER(31,"主叫"),

    /**
     * 被叫
     */
    CALLEE(32, "被叫"),
    ;

    private final Integer value;

    private final String name;

    public static final List<UserChatParticipantTypeEnum> OWNER_TYPE = List.of(OWNER, CO_OWNER);
}
