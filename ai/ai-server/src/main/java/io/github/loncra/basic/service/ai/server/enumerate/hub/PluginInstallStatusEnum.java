package io.github.loncra.basic.service.ai.server.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginInstallStatusEnum implements NameValueEnum<Integer> {

    PENDING("待激活", 10),

    ACTIVATED("已激活", 20),

    DISABLED("已禁用", 30),
    ;

    private final String name;

    private final Integer value;
}
