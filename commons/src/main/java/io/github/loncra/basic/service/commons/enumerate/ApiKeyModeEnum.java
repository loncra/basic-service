package io.github.loncra.basic.service.commons.enumerate;

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
public enum ApiKeyModeEnum implements NameValueEnum<Integer> {

    MANUAL("手动输入", 10),

    OAUTH("OAuth", 20),

    NONE("无", 30),
    ;

    private final String name;

    private final Integer value;
}
