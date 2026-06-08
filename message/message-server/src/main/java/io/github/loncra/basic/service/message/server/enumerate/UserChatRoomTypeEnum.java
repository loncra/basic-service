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
public enum UserChatRoomTypeEnum implements NameValueEnum<Integer> {

    /**
     * 群聊
     */
    GROUP_CHAT("群聊", 10),

    /**
     * 单聊
     */
    PRIVATE_CHAT("单聊", 20);

    ;

    private final String name;

    private final Integer value;
}
