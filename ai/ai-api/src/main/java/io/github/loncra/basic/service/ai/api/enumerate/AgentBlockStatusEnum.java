package io.github.loncra.basic.service.ai.api.enumerate;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentBlockStatusEnum implements NameValueEnum<String> {

    RUNNING("进行中", "running"),

    DONE("完成", "done"),

    FAILED("失败", "failed"),
    ;

    private final String name;

    private final String value;
}
