package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PackageOriginEnum implements NameValueEnum<Integer> {

    INTERNAL("内部", 10),

    EXTERNAL("外部", 20),

    ;

    private final String name;

    private final Integer value;
}
