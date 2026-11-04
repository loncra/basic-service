package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExecutionChannelEnum implements NameValueEnum<String> {

    MYSQL("mysql", "数据库执行引用"),

    REDIS("redis", "缓存执行引用");

    private final String value;

    private final String name;
}
