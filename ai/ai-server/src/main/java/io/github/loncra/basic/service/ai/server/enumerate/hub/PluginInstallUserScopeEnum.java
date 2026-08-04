package io.github.loncra.basic.service.ai.server.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginInstallUserScopeEnum implements NameValueEnum<Integer> {

    USER("个人用户", 10),

    ORG("企业用户", 20),

    ;

    private final String name;

    private final Integer value;
}
