package io.github.loncra.basic.service.message.server.enumerate.chat.call;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通话状态
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatCallStatusEnum implements NameValueEnum<Integer> {

    CONNECTING(10, "链接中"),

    ACTIVE(20, "通话中"),

    COMPLETED(30, "通话结束")

    ;

    private final Integer value;

    private final String name;
}
