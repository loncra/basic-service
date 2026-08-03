package io.github.loncra.basic.service.ai.server.service;

import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.dao.ModelSettingDao;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.basic.service.commons.domain.metadata.TreeSortMetadata;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 *
 * tb_model_setting 的业务逻辑
 *
 * <p>Table: tb_model_setting - 模型配置</p>
 *
 * @see ModelSettingEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-30 07:51:00
 */
@Service
@RequiredArgsConstructor
public class ModelSettingService extends BasicService<ModelSettingDao, ModelSettingEntity> {

    private final List<ModelResolver> modelResolvers;

    public ModelResolverMetadata getModelMetadata(
            ModelSettingMetadata model,
            Map<String, Object> options
    ) {

        return modelResolvers.stream()
                .filter(r -> r.support(model))
                .findFirst()
                .orElseThrow(() -> new SecurityException("找不到厂商为 [" + model.getManufacturer().getName() + "] 模型为 [" + model.getName() + "] 解析器"))
                .resolve(model, options);
    }

    @Transactional(rollbackFor = Exception.class)
    public void sort(List<TreeSortMetadata<Long>> sorts) {
        for (TreeSortMetadata<Long> sort : sorts) {
            lambdaUpdate().set(ModelSettingEntity::getSort, sort.getSort())
                    .eq(ModelSettingEntity::getId, sort.getId())
                    .update();
        }
    }

}
