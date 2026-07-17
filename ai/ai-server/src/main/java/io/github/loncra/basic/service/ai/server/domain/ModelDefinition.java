package io.github.loncra.basic.service.ai.server.domain;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;

import java.util.Map;

public interface ModelDefinition extends NameValueEnum<Integer> {

    /**
     * 获取默认配置
     *
     * @return 默认配置
     */
    Map<String, Object> getDefaultOptions();
}
