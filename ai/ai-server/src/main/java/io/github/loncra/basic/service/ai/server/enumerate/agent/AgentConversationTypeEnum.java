package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentConversationTypeEnum implements NameValueEnum<Integer> {

    DEFAULT_WORKSPACE("默认工作空间",10),

    CUSTOMIZE_WORKSPACE("自定义工作空间",20),

    WORKSPACE_CONVERSATION("工作空间会话",30);

    private final String name;

    private final Integer value;
}
