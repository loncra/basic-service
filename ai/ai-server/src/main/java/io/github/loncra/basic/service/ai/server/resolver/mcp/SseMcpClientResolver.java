package io.github.loncra.basic.service.ai.server.resolver.mcp;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.agentscope.core.tool.mcp.McpSyncClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.mcp.McpClientSseTransportMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.McpPackageResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.net.http.HttpRequest;


@Component
public class SseMcpClientResolver implements McpPackageResolver {

    public static final String DEFAULT_ENDPOINT = "/mcp";

    @Override
    public boolean isSupport(String type) {
        return McpClientTypeEnum.SSE.toString().equals(type);
    }

    @Override
    public McpClientWrapper resolve(String name, AbstractMcpClientTransportMetadata metadata) {
        McpClientSseTransportMetadata sse = CastUtils.cast(metadata);
        String endpoint = StringUtils.defaultIfEmpty(sse.getEndpoint(), DEFAULT_ENDPOINT);
        if (MapUtils.isNotEmpty(sse.getQueryParams())) {
            String param = HttpRequestParameterMapUtils.castRequestBodyMapToString(new LinkedMultiValueMap<>(sse.getQueryParams()));
            endpoint += HttpRequestParameterMapUtils.QUESTION_MARK + param;
        }

        HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport
                .builder(sse.getBaseUrl())
                .sseEndpoint(endpoint);

        builder.httpRequestCustomizer((httpBuilder, method, url, body, context) -> buildRequest(httpBuilder, sse));
        HttpClientSseClientTransport transport = builder.build();
        McpSyncClient client = McpClient.sync(transport)
                .capabilities(McpSchema.ClientCapabilities.builder().build())
                .build();
        return new McpSyncClientWrapper(name, client);
    }

    private void buildRequest(
            HttpRequest.Builder builder,
            McpClientSseTransportMetadata metadata
    ) {
        if (MapUtils.isNotEmpty(metadata.getHeaders())) {
            metadata.getHeaders().forEach((k, v) -> v.forEach(value -> builder.header(k, value)));
        }
    }
}
