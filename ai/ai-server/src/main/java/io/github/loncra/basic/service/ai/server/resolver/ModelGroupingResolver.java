package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.enumerate.ModelTypeEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;

import java.util.List;

public interface ModelGroupingResolver {

    /**
     * 获取所属分组
     *
     * @return 所属分组
     */
    ModelTypeEnum getGrouping();

    /**
     * 获取模型映射元数据
     *
     * @return 模型映射元数据集合
     */
    List<IdValueMetadata<String, String>> getModelMetadata();
}
