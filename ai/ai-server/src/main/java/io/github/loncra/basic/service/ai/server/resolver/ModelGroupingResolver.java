package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.enumerate.ModelTypeEnum;

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
    List<ModelSettingEntity> getModelMetadata();
}
