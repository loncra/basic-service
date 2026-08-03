package io.github.loncra.basic.service.ai.server.resolver.mcp;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.agentscope.core.tool.mcp.McpSyncClientWrapper;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.TavilyConfig;
import io.github.loncra.basic.service.ai.server.enumerate.ToolGroupEnum;
import io.github.loncra.basic.service.ai.server.resolver.McpClientResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@RequiredArgsConstructor
public class TavilyMcpClientResolver implements McpClientResolver {

    public static final String NAME = "tavily";

    private final AiAppConfig aiAppConfig;

    private final TavilyConfig tavilyConfig;

    @Override
    public McpClientWrapper getClient() {
        String apiKey = aiAppConfig.getKey().get(NAME);
        SystemException.isTrue(StringUtils.isNoneEmpty(apiKey), "Tavily API key must not be blank");
        String defaultParams = SystemException.convertSupplier(
                () -> CastUtils.getObjectMapper().writeValueAsString(tavilyConfig.getDefaultParams()));
        String endpoint = MessageFormat.format(tavilyConfig.getEndpoint(), apiKey);
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(tavilyConfig.getBaseUrl())
                .endpoint(endpoint)
                .openConnectionOnStartup(false)
                .resumableStreams(false)
                .httpRequestCustomizer((builder, method, uri, body, ctx) ->
                        builder.header(tavilyConfig.getDefaultParamsHeaderField(), defaultParams))
                .build();
        McpSyncClient client = McpClient.sync(transport)
                .capabilities(McpSchema.ClientCapabilities.builder().build())
                .build();
        return new McpSyncClientWrapper(NAME, client);
    }

    @Override
    public ToolGroupEnum getGroup() {
        return ToolGroupEnum.EXPLORE;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

}
