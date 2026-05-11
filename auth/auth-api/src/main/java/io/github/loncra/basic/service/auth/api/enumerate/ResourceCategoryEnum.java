package io.github.loncra.basic.service.auth.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ResourceCategoryEnum implements NameValueEnum<Integer> {

    PLUGIN(10, "插件"),

    CUSTOMIZE(20, "自定义")

    ;

    private final Integer value;

    private final String name;
}