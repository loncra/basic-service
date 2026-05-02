package io.github.loncra.basic.service.commons.enumerate;

import io.github.loncra.framework.commons.annotation.GetValueStrategy;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@GetValueStrategy(type = GetValueStrategy.Type.ToString)
public enum RuntimeModeEnum implements NameEnum {

    /**
     * 未知类型
     */
    MONOLITH("单体服务"),
    /**
     * 未知类型
     */
    MICROSERVICE("微服务"),
    ;


    private final String name;

}
