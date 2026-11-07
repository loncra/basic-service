package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ExecutionChannelEnum implements NameValueEnum<String> {

    CLOUD("cloud", "云端执行通道"),

    LOCAL("local", "本地执行通道");

    private final String value;

    private final String name;
}
