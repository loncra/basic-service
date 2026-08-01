package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ToolHitlStatusEnum implements NameValueEnum<String> {
    NONE("无需人工介入", "none"),
    WAITING_CONFIRM("等待用户确认", "waiting_confirm"),
    WAITING_EXTERNAL("等待外部执行", "waiting_external"),
    ;

    private final String name;

    private final String value;
}
