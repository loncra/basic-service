package io.github.loncra.basic.service.ai.server.domain;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.ClarifyPluginMetadata;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class NoCloseMcpClientWrapper extends McpClientWrapper {

    private final McpClientWrapper delegate;

    @Getter
    private final boolean dynamicActivation;

    @Getter
    private final ClarifyPluginMetadata metadata;

    public NoCloseMcpClientWrapper(
            McpClientWrapper delegate,
            ClarifyPluginMetadata metadata,
            boolean dynamicActivation
    ) {
        super(delegate.getName());
        this.delegate = delegate;
        this.metadata = metadata;
        this.dynamicActivation = dynamicActivation;
    }


    @Override
    public Mono<Void> initialize() {
        return delegate.initialize();
    }

    @Override
    public Mono<List<McpSchema.Tool>> listTools() {
        return delegate.listTools();
    }

    @Override
    public Mono<McpSchema.CallToolResult> callTool(
            String toolName,
            Map<String, Object> arguments
    ) {
        return delegate.callTool(toolName, arguments);
    }

    @Override
    public Mono<McpSchema.CallToolResult> callTool(
            String toolName,
            Map<String, Object> arguments,
            Map<String, Object> meta
    ) {
        return delegate.callTool(toolName, arguments, meta);
    }

    @Override
    public void close() {
        // 不做任何动作
    }

    public void closeDelegate() {
        delegate.close();
    }


}
