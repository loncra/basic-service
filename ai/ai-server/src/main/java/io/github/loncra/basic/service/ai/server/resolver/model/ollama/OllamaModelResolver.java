package io.github.loncra.basic.service.ai.server.resolver.model.ollama;

import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.ollama.OllamaChatModel;
import io.agentscope.extensions.model.ollama.options.OllamaOptions;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class OllamaModelResolver implements ModelResolver {

    private static final String MODEL_NAME = "ollama";

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
            return OllamaChatModel.builder()
                    .modelName(model.getModel())
                    .formatter(new OllamaChatThinkingFormatter())
                    .build();
        } else {
            Object defaults = model.getMetadata().getOrDefault(ModelSettingMetadata.MODEL_DEFAULT_OPTIONS_KEY, new LinkedHashMap<>());

            OllamaOptions ollamaOptions = buildOptions(
                    CastUtils.convertValue(defaults, CastUtils.MAP_TYPE_REFERENCE),
                    options,
                    OllamaOptions.class
            );

            return OllamaChatModel.builder()
                    .modelName(model.getModel())
                    .defaultOptions(ollamaOptions)
                    .formatter(new OllamaChatThinkingFormatter())
                    .build();
        }
    }
}
