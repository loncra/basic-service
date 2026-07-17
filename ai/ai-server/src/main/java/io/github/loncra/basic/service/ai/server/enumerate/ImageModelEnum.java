package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.basic.service.ai.server.domain.ModelDefinition;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 生图模型枚举
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ImageModelEnum implements ModelDefinition {

    GOOGLE("谷歌", 10, Map.of()),
    ;

    private final String name;

    private final Integer value;

    private final Map<String, Object> defaultOptions;
}
