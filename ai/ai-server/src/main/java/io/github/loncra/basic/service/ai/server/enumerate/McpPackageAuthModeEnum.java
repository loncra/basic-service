package io.github.loncra.basic.service.ai.server.enumerate;

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
public enum McpPackageAuthModeEnum implements NameValueEnum<Integer> {

    API_KEY("apiKey", 10),

    OAUTH("OAuth", 20),
    ;

    private final String name;

    private final Integer value;
}
