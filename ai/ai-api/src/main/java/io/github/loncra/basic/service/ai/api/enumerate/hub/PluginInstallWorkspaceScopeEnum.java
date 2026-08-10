package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginInstallWorkspaceScopeEnum implements NameValueEnum<Integer> {

    USER("所有工作空间", 10),

    ORG("指定工作空间", 20),

    ;

    private final String name;

    private final Integer value;
}
