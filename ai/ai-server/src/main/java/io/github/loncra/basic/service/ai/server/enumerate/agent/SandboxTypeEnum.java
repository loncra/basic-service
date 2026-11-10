package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SandboxTypeEnum implements NameValueEnum<String> {

    DIRECTORY("directory", "本机目录"),

    DOCKER("docker", "Docker 沙箱");

    private final String value;

    private final String name;
}
