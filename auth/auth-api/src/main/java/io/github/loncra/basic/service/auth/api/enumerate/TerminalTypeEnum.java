package io.github.loncra.basic.service.auth.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TerminalTypeEnum implements NameEnum {

    CLIENT("客户端"),

    BROWSER("浏览器"),

    ;

    private final String name;
}
