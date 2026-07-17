package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.basic.service.ai.server.domain.ModelDefinition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 聊天模型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChatModelEnum implements ModelDefinition {

    /**
     * ollama
     */
    OLLAMA("ollama", 10, Map.of(ChatModelEnum.THINK_OPTION_KEY, true)),

    /**
     * 百炼平台
     */
    DASH_SCOPE("dashscope", 20, Map.of(ChatModelEnum.THINK_OPTION_KEY, true)),

    /**
     * open ai
     */
    OPEN_AI("openai", 30, Map.of(ChatModelEnum.THINK_OPTION_KEY, true)),


    GOOGLE("google", 40, Map.of(ChatModelEnum.THINK_OPTION_KEY, true))
    ;

    private final String name;

    private final Integer value;

    private final Map<String, Object> defaultOptions;


    public static final String THINK_OPTION_KEY = "thinkOption";

}
