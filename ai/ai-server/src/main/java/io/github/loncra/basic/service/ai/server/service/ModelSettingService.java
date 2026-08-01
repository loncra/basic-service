package io.github.loncra.basic.service.ai.server.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.permission.PermissionBehavior;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.permission.PermissionRule;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.dao.ModelSettingDao;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
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

    private final Map<Long, HarnessAgent> harnessAgentCache = new ConcurrentHashMap<>();

    private final Map<Long, ReActAgent> reActAgentCache = new ConcurrentHashMap<>();

    private final AiAppConfig aiAppConfig;

    private final AgentStateStore agentStateStore;

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

    public ReActAgent getRcActAgent(ModelSettingMetadata model) {
        return getRcActAgent(model, null);
    }

    public ReActAgent getRcActAgent(
            ModelSettingMetadata model,
            Map<String, Object> metadata
    ) {
        return reActAgentCache.computeIfAbsent(model.getId(), k -> createReActAgent(model, metadata));
    }

    public HarnessAgent getHarnessAgent(ModelSettingMetadata model) {
        return getHarnessAgent(model, null);
    }

    public HarnessAgent getHarnessAgent(
            ModelSettingMetadata model,
            Map<String, Object> metadata
    ) {
        return harnessAgentCache.computeIfAbsent(model.getId(), k -> createHarnessAgent(model, metadata));
    }

    @Override
    public int updateById(ModelSettingEntity entity) {
        HarnessAgent harnessAgent = harnessAgentCache.remove(entity.getId());
        if (Objects.nonNull(harnessAgent)) {
            harnessAgent.close();
        }

        ReActAgent reActAgent = reActAgentCache.remove(entity.getId());
        if (Objects.nonNull(reActAgent)) {
            reActAgent.close();
        }
        return super.updateById(entity);
    }

    private HarnessAgent createHarnessAgent(
            ModelSettingMetadata model,
            Map<String, Object> metadata
    ) {
        ModelResolverMetadata resolverMetadata = getModelMetadata(model, metadata);
        CompactionConfig.builder().build();
        return HarnessAgent.builder()
                .name(HarnessAgent.class.getSimpleName() + CastUtils.UNDERSCORE + model.getId())
                .model(resolverMetadata.getModel())
                .toolkit(resolverMetadata.getToolkit())
                .sysPrompt(aiAppConfig.getSystemPrompt())
                .stateStore(agentStateStore)
                .compaction(aiAppConfig.toCompactionConfig())
                .workspace(aiAppConfig.getWorkspacePath())
                .build();
    }

    private ReActAgent createReActAgent(
            ModelSettingMetadata model,
            Map<String, Object> metadata
    ) {
        ModelResolverMetadata resolverMetadata = getModelMetadata(model, metadata);

        return ReActAgent.builder()
                .name(model.getManufacturer().getName() + CacheProperties.DEFAULT_SEPARATOR + model.getName())
                .model(resolverMetadata.getModel())
                //.toolkit(resolverMetadata.getToolkit())
                .build();
    }
}
