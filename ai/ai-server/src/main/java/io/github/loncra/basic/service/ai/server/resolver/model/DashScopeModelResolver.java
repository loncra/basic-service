package io.github.loncra.basic.service.ai.server.resolver.model;

import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DashScopeModelResolver implements ModelResolver {

    private static final String MODEL_NAME = "dashScope";

    private final AiAppConfig aiAppConfig;

    @Override
    public boolean support(ModelSettingMetadata model) {
        return MODEL_NAME.equals(model.getManufacturer().getValue());
    }

    @Override
    public Model resolve(
            ModelSettingMetadata model,
            Map<String, Object> options
    ) {
        if (Objects.isNull(options)) {
            return DashScopeChatModel.builder()
                    .apiKey(aiAppConfig.getKey().get(MODEL_NAME))
                    .enableThinking(false)
                    .modelName(model.getModel())
                    .build();
        } else {
            Object defaults = model.getMetadata().getOrDefault(ModelSettingMetadata.MODEL_DEFAULT_OPTIONS_KEY, new LinkedHashMap<>());

            GenerateOptions generateOptions = buildGenerateOptions(
                    CastUtils.convertValue(defaults, CastUtils.MAP_TYPE_REFERENCE),
                    options
            );

            return DashScopeChatModel.builder()
                    .apiKey(aiAppConfig.getKey().get(MODEL_NAME))
                    .modelName(model.getModel())
                    .enableThinking(Objects.nonNull(generateOptions.getThinkingBudget()))
                    .defaultOptions(generateOptions)
                    .build();
        }
    }
}
