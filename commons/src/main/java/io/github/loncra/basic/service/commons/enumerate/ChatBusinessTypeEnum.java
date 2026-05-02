package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai chat 业务类型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChatBusinessTypeEnum implements NameValueEnum<Integer> {

    /**
     * 聊天线程类型
     */
    CHAT_MESSAGE("基本推理行动反馈类型", 10),

    /**
     * 剧集生成类型
     */
    VIDEO_ASSEMBLY("短剧制作类型", 20),
    ;

    private final String name;

    private final Integer value;
}
