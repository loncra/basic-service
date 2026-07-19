package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentChatTypeEnum implements NameValueEnum<Integer> {

    ASK("问答",10),

    PLAN("计划",20),

    AGENT("智能体",30);

    private final String name;

    private final Integer value;
}
