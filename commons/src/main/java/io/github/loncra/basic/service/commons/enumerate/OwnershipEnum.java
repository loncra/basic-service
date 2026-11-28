package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OwnershipEnum implements NameValueEnum<Integer> {

    USER("个人", 10),

    ENTERPRISE("企业", 20),

    ;

    private final String name;

    private final Integer value;
}
