package io.github.loncra.basic.service.ai.server.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.dao.ModelSettingDao;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Long, ReActAgent> agentCache = new ConcurrentHashMap<>();

    public Model getModel(
            ModelSettingMetadata model,
            Map<String, Object> options
    ) {

        return modelResolvers.stream()
                .filter(r -> r.support(model))
                .findFirst()
                .orElseThrow(() -> new SecurityException("找不到厂商为 [" + model.getManufacturer().getName() + "] 模型为 [" + model.getName() + "] 解析器"))
                .resolve(model, options);
    }

    public ReActAgent getAgent(ModelSettingMetadata model) {
        return getAgent(model, null);
    }

    public ReActAgent getAgent(
            ModelSettingMetadata model,
            Map<String, Object> metadata
    ) {
        return agentCache.computeIfAbsent(model.getId(), k -> createAgent(model, metadata));
    }

    @Override
    public int updateById(ModelSettingEntity entity) {
        ReActAgent agent = agentCache.remove(entity.getId());
        if (Objects.nonNull(agent)) {
            agent.close();
        }
        return super.updateById(entity);
    }

    private ReActAgent createAgent(
            ModelSettingMetadata model,
            Map<String, Object> metadata
    ) {
        return ReActAgent.builder()
                .name(model.getManufacturer().getName() + CacheProperties.DEFAULT_SEPARATOR + model.getName())
                .model(getModel(model, metadata))
                .toolkit(new Toolkit())
                .build();
    }
}
