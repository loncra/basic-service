package io.github.loncra.basic.service.ai.server.resolver.model;

import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.model.ModelResolverMetadata;
import io.github.loncra.basic.service.ai.server.resolver.ModelResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Setter(onMethod_ = @Autowired)
public abstract class AbstractModelResolver implements ModelResolver {

    @Getter(AccessLevel.NONE)
    private AiMcpPackageService aiMcpPackageService;

    @Override
    public ModelResolverMetadata resolve(
            ModelSettingMetadata modelSetting,
            Map<String, Object> options
    ) {
        Model model  = createModel(modelSetting,options);

        ModelResolverMetadata modelResolverMetadata = new ModelResolverMetadata();
        modelResolverMetadata.setModel(model);

        Toolkit toolkit = new Toolkit();
        toolkit.registerMetaTool();

        List<AiMcpPackageEntity> packages = aiMcpPackageService.findSystemMcpPackage();
        for (AiMcpPackageEntity p : packages) {
            Optional<McpClientWrapper> optional = aiMcpPackageService.convertMcpClientWrapper(p);
            if (optional.isEmpty()) {
                continue;
            }
            McpClientWrapper mcpClientWrapper = optional.get();
            if (Objects.isNull(p.getGroup())) {
                toolkit.registration()
                        .mcpClient(mcpClientWrapper)
                        .apply();
            } else {
                String group = Objects.toString(p.getGroup().getValue(), BeanDefinitionParserDelegate.DEFAULT_VALUE);
                // 组不存在就建；active=false → 首轮 schema 不暴露组内 MCP tools
                if (Objects.isNull(toolkit.getToolGroup(group))) {
                    toolkit.createToolGroup(
                            group,
                            p.getGroup().getName(),
                            false
                    );
                }

                toolkit.registration()
                        .mcpClient(mcpClientWrapper)
                        .group(group)
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
