package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginInstallLayerEnum implements NameValueEnum<Integer> {

    ENTERPRISE_ROW("企业安装", 10),

    WORKSPACE("工作空间安装", 20),

    USER_ROW("个人安装", 30),

    ;

    private final String name;

    private final Integer value;
}
