package io.github.loncra.basic.service.ai.server.resolver.mcp;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.agentscope.core.tool.mcp.McpSyncClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.mcp.McpClientStreamableHttpTransportMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.McpPackageResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.net.http.HttpRequest;


@Component
public class StreamableHttpMcpClientResolver implements McpPackageResolver {

    public static final String DEFAULT_ENDPOINT = "/mcp";

    @Override
    public boolean isSupport(String type) {
        return McpClientTypeEnum.STREAMABLE_HTTP.toString().equals(type);
    }

    @Override
    public McpClientWrapper resolve(String name, AbstractMcpClientTransportMetadata metadata) {
        McpClientStreamableHttpTransportMetadata streamableHttp = CastUtils.cast(metadata);
        String endpoint = StringUtils.defaultIfEmpty(streamableHttp.getEndpoint(), DEFAULT_ENDPOINT);
        if (MapUtils.isNotEmpty(streamableHttp.getQueryParams())) {
            String param = HttpRequestParameterMapUtils.castRequestBodyMapToString(new LinkedMultiValueMap<>(streamableHttp.getQueryParams()));
            endpoint += HttpRequestParameterMapUtils.QUESTION_MARK + param;
        }
        HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport
                .builder(streamableHttp.getBaseUrl())
                .endpoint(endpoint);


        builder.openConnectionOnStartup(streamableHttp.isOpenConnectionOnStartup());
        builder.resumableStreams(streamableHttp.isResumableStreams());

        builder.httpRequestCustomizer((httpBuilder, method, uri, body, context) -> buildRequest(httpBuilder, streamableHttp));
        HttpClientStreamableHttpTransport transport = builder.build();
        McpSyncClient client = McpClient.sync(transport)
                .capabilities(McpSchema.ClientCapabilities.builder().build())
                .build();
        return new McpSyncClientWrapper(name, client);
    }

    private void buildRequest(
            HttpRequest.Builder builder,
            McpClientStreamableHttpTransportMetadata metadata
    ) {
        if (MapUtils.isNotEmpty(metadata.getHeaders())) {
            metadata.getHeaders().forEach((k, v) -> v.forEach(value -> builder.header(k, value)));
        }
    }
}
