package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.AbstractMcpClientTransportMetadata;

public interface McpPackageResolver {

    boolean isSupport(String type);

    McpClientWrapper resolve(String name, AbstractMcpClientTransportMetadata metadata);
}
