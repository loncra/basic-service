package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DefaultOperateCategoryEnum implements NameValueEnum<Integer> {

    SYSTEM(10, "系统"),

    CUSTOMIZE(20, "自定义")

    ;

    private final Integer value;

    private final String name;
}