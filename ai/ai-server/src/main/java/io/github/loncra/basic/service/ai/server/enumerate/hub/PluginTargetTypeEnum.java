package io.github.loncra.basic.service.ai.server.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginTargetTypeEnum implements NameValueEnum<Integer> {

    SYSTEM("技能", 10),

    HUB("mcp", 20),

    ;

    private final String name;

    private final Integer value;
}
