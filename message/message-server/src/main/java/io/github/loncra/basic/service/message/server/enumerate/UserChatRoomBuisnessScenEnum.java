package io.github.loncra.basic.service.message.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天房间类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatRoomBuisnessScenEnum implements NameValueEnum<Integer> {

    /**
     * 即时聊天
     */
    IM("即时聊天", 10),

    ;

    private final String name;

    private final Integer value;
}
