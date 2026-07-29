package io.github.loncra.basic.service.ai.server.resolver.model.ollama;

import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.extensions.model.ollama.OllamaChatModel;
import io.agentscope.extensions.model.ollama.options.OllamaOptions;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OllamaModelResolver implements ModelResolver {

    private static final String MODEL_NAME = "ollama";

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
            model = OllamaChatModel.builder()
                    .modelName(modelSettingMetadata.getModel())
                    .formatter(new OllamaChatThinkingFormatter())
                    .build();
        } else {
            Object defaults = modelSettingMetadata.getMetadata().getOrDefault(ModelSettingMetadata.MODEL_DEFAULT_OPTIONS_KEY, new LinkedHashMap<>());

            OllamaOptions ollamaOptions = buildOptions(
                    CastUtils.convertValue(defaults, CastUtils.MAP_TYPE_REFERENCE),
                    options,
                    OllamaOptions.class
            );

            model = OllamaChatModel.builder()
                    .modelName(modelSettingMetadata.getModel())
                    .defaultOptions(ollamaOptions)
                    .formatter(new OllamaChatThinkingFormatter())
                    .build();
        }
        modelResolverMetadata.setModel(model);;
        Toolkit defaults = new Toolkit();
        defaults.registration().mcpClient(
                McpClientBuilder.create("mcp-server-firecrawl")
                        .stdioTransport("npx.cmd", List.of("-y", "firecrawl-mcp"), Map.of("FIRECRAWL_API_KEY", aiAppConfig.getKey().get("firecrawl")))
                        .buildSync()
        ).apply();
        modelResolverMetadata.setToolkit(defaults);
        return modelResolverMetadata;
    }
}
