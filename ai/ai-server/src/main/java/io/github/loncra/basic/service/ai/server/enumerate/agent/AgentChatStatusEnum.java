package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentChatStatusEnum implements NameValueEnum<Integer> {

    READY(10, "就绪"),

    RUNNING(20, "生成中"),

    STOPPED(30, "已停止"),

    COMPLETED(35, "已完成"),

    FAILED(40, "失败");

    private final Integer value;

    private final String name;

    public static final List<AgentChatStatusEnum> COMPLETED_STATUS = List.of(COMPLETED, FAILED);
}
