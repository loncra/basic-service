package io.github.loncra.basic.service.ai.server.enumerate.agent;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentTokenUsageTypeEnum implements NameValueEnum<String> {

    MODEL_COMPLETED("模型执行完成", "modelCompleted"),

    GENERATE_CONVERSATION_NAME("生成会话名称", "generateConversationName"),

    ;

    private final String name;

    private final String value;
}
