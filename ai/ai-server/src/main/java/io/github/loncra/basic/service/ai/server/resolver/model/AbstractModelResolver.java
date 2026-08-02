package io.github.loncra.basic.service.ai.server.resolver.model;

import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.resolver.McpClientResolver;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Setter(onMethod_ = @Autowired)
public abstract class AbstractModelResolver implements ModelResolver {

    @Getter(AccessLevel.NONE)
    private List<McpClientResolver> mcpClientResolver;

    @Override
    public ModelResolverMetadata resolve(
            ModelSettingMetadata modelSetting,
            Map<String, Object> options
    ) {
        Model model  = createModel(modelSetting,options);

        ModelResolverMetadata modelResolverMetadata = new ModelResolverMetadata();
        modelResolverMetadata.setModel(model);

        Toolkit defaults = new Toolkit();

        mcpClientResolver.stream()
                .filter(McpClientResolver::isRequired)
                .forEach(s -> defaults.registration().mcpClient(s.getMcpClient()).apply());
        modelResolverMetadata.setToolkit(defaults);

        return modelResolverMetadata;
    }

    protected abstract Model createModel(
            ModelSettingMetadata modelSetting,
            Map<String, Object> options
    );
}
