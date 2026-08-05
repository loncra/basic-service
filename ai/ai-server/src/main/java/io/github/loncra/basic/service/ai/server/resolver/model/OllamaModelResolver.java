package io.github.loncra.basic.service.ai.server.resolver.model;

import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.ollama.OllamaChatModel;
import io.agentscope.extensions.model.ollama.options.OllamaOptions;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OllamaModelResolver extends AbstractModelResolver {

    public static final String MODEL_NAME = "ollama";

    @Override
    public boolean support(ModelSettingMetadata model) {
        return MODEL_NAME.equals(model.getManufacturer().getValue());
    }

    @Override
    protected Model createModel(
            ModelSettingMetadata modelSetting,
            Map<String, Object> options
    ) {
        Model model;
        if (Objects.isNull(options)) {
            model = OllamaChatModel.builder()
                    .modelName(modelSetting.getModel())
                    .build();
        } else {
            Object defaults = modelSetting.getMetadata().getOrDefault(ModelSettingMetadata.MODEL_DEFAULT_OPTIONS_KEY, new LinkedHashMap<>());

            OllamaOptions ollamaOptions = buildOptions(
                    CastUtils.convertValue(defaults, CastUtils.MAP_TYPE_REFERENCE),
                    options,
                    OllamaOptions.class
            );

            model = OllamaChatModel.builder()
                    .modelName(modelSetting.getModel())
                    .defaultOptions(ollamaOptions)
                    .build();
        }

        return model;
    }
}
