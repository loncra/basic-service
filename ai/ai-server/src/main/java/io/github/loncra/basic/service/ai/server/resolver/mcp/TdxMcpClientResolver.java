package io.github.loncra.basic.service.ai.server.resolver.mcp;

import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.TdxConfig;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientGroupEnum;
import io.github.loncra.basic.service.ai.server.resolver.McpClientResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TdxMcpClientResolver implements McpClientResolver {

    public static final String NAME = "tdx";

    private final TdxConfig tdxConfig;

    private final AiAppConfig aiAppConfig;

    @Override
    public McpClientWrapper getMcpClient() {
        return McpClientBuilder.create(NAME)
                .sseTransport(tdxConfig.getBaseUrl())
                .header(tdxConfig.getApiKeyField(), aiAppConfig.getKey().get(NAME))
                .buildSync();
    }

    @Override
    public McpClientGroupEnum getGroup() {
        return McpClientGroupEnum.EXPLORE;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
