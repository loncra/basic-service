package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;
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
public enum McpClientResultAssertionTypeEnum implements NameEnum {

    TEXT("text"),

    JSON("json"),
    ;

    private final String name;
}
