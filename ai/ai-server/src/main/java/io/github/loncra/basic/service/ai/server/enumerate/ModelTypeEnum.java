package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ModelTypeEnum implements NameValueEnum<Integer> {

    CHAT("聊天模型", 10),

    IMAGE("生图模型", 20),

    VIDEO("视频模型", 30),

    VOICE("人声模型", 40),

    MUSIC("音乐模型", 50),
    ;

    private final String name;

    private final Integer value;
}
