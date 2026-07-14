package io.github.loncra.basic.service.message.server.enumerate.chat.call;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通话类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserChatCallTypeEnum implements NameValueEnum<Integer> {

    /**
     * 视频通话
     */
    VIDEO("视频通话", 10),

    /**
     * 单聊
     */
    VOICE("语音通话", 20);

    ;

    private final String name;

    private final Integer value;
}
