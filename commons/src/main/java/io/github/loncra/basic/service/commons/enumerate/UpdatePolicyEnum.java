package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UpdatePolicyEnum implements NameValueEnum<Integer> {

    /**
     * 手动更新
     */
    MANUAL(10, "手动更新"),

    /**
     * 自动更新
     */
    AUTOMATIC(20, "自动更新"),

    ;

    private final Integer value;

    private final String name;
}
