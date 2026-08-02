package io.github.loncra.basic.service.ai.server.resolver.model;

import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.OpenAIChatFormatter;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DeepSeekModelResolver extends AbstractModelResolver {

    public static final String MODEL_NAME = "deepSeek";

    public static final String BASE_URL = "https://api.deepseek.com";

    private final AiAppConfig aiAppConfig;

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
            model = OpenAIChatModel.builder()
                    .apiKey(aiAppConfig.getKey().get(MODEL_NAME))
                    .modelName(modelSetting.getModel())
                    .baseUrl(BASE_URL)
                    .formatter(new OpenAIChatFormatter())
                    .build();
        } else {
            Object defaults = modelSetting.getMetadata()
                    .getOrDefault(ModelSettingMetadata.MODEL_DEFAULT_OPTIONS_KEY, new LinkedHashMap<>());

            GenerateOptions generateOptions = buildGenerateOptions(
                    CastUtils.convertValue(defaults, CastUtils.MAP_TYPE_REFERENCE),
                    options
            );

            model = OpenAIChatModel.builder()
                    .apiKey(aiAppConfig.getKey().get(MODEL_NAME))
                    .modelName(modelSetting.getModel())
                    .baseUrl(BASE_URL)
                    .generateOptions(generateOptions)
                    .formatter(new OpenAIChatFormatter())
                    .build();
        }

        return model;
    }
}
