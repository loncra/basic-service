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
import java.util.Objects;

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

        Toolkit toolkit = new Toolkit();

        // 让模型能自己开关工具组（上下文里几乎只有这一个 meta tool + 组名说明）
        toolkit.registerMetaTool();
        for (McpClientResolver resolver : mcpClientResolver) {
            if (!resolver.isRequired()) {
                continue;
            }
            if (Objects.isNull(resolver.getGroup())) {
                toolkit.registration()
                        .mcpClient(resolver.getClient())
                        .apply();
            } else {
                // 组不存在就建；active=false → 首轮 schema 不暴露组内 MCP tools
                if (Objects.isNull(toolkit.getToolGroup(resolver.getGroup().getValue()))) {
                    toolkit.createToolGroup(
                            resolver.getGroup().getValue(),
                            // 「探索」「互联网搜索」
                            resolver.getGroup().getName(),
                            false
                    );
                }
                toolkit.registration()
                        .mcpClient(resolver.getClient())
                        .group(resolver.getGroup().getValue())
                        .apply();
            }
        }

        modelResolverMetadata.setToolkit(toolkit);

        return modelResolverMetadata;
    }

    protected abstract Model createModel(
            ModelSettingMetadata modelSetting,
            Map<String, Object> options
    );
}
