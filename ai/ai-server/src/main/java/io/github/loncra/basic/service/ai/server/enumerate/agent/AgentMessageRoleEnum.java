package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentMessageRoleEnum implements NameValueEnum<String> {

    USER("user", "用户"),

    ASSISTANT("ai", "助手");

    private final String value;

    private final String name;
}
