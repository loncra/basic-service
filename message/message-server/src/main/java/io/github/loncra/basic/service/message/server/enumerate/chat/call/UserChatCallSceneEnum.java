package io.github.loncra.basic.service.message.server.enumerate.chat.call;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 通话状态
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatCallSceneEnum implements NameValueEnum<Integer> {

    PRIVATE(10, "私聊"),

    GROUP(20, "群聊"),

    MEETING(30, "会议"),

    ;

    private final Integer value;

    private final String name;

    public static final List<UserChatCallSceneEnum> ACCEPT_SCENE = Arrays.asList(PRIVATE, MEETING);
}
