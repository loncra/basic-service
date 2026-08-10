package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PackageTypeEnum implements NameValueEnum<Integer> {

    SYSTEM("系统", 10),

    HUB("广场", 20),

    ;

    private final String name;

    private final Integer value;
}
