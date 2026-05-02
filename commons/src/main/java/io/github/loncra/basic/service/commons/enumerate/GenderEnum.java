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
public enum GenderEnum implements NameValueEnum<Integer> {

    /**
     * 男
     */
    MALE(10, "男"),

    /**
     * 女
     */
    FEMALE(20, "女"),

    /**
     * 未设置性别
     */
    UNKNOWN(30, "未设置性别");

    private final Integer value;

    private final String name;
}
