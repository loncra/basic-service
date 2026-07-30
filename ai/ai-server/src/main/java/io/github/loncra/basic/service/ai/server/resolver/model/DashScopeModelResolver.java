package io.github.loncra.basic.service.ai.server.resolver.model;

import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
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
    public ModelResolverMetadata resolve(
            ModelSettingMetadata modelSettingMetadata,
            Map<String, Object> options
    ) {
        ModelResolverMetadata modelResolverMetadata = new ModelResolverMetadata();
        Model model;
        if (Objects.isNull(options)) {
            model =  DashScopeChatModel.builder()
                    .apiKey(aiAppConfig.getKey().get(MODEL_NAME))
                    .enableThinking(false)
                    .enableThinking(false)
                    .modelName(modelSettingMetadata.getModel())
                    .build();
        } else {
            Object defaults = modelSettingMetadata.getMetadata().getOrDefault(ModelSettingMetadata.MODEL_DEFAULT_OPTIONS_KEY, new LinkedHashMap<>());

            GenerateOptions generateOptions = buildGenerateOptions(
                    CastUtils.convertValue(defaults, CastUtils.MAP_TYPE_REFERENCE),
                    options
            );

            model = DashScopeChatModel.builder()
                    .apiKey(aiAppConfig.getKey().get(MODEL_NAME))
                    .modelName(modelSettingMetadata.getModel())
                    .enableSearch(true)
                    .enableThinking(Objects.nonNull(generateOptions.getThinkingBudget()))
                    .defaultOptions(generateOptions)
                    .build();
        }
        modelResolverMetadata.setModel(model);
        Toolkit defaults = new Toolkit();

        /*defaults.registration()
                .tool(new DashScopeMultiModalTool(aiAppConfig.getKey().get(MODEL_NAME)))
                .group(BeanDefinitionParserDelegate.DEFAULT_VALUE)
                .apply();*/
        modelResolverMetadata.setToolkit(defaults);

        return modelResolverMetadata;
    }
}
