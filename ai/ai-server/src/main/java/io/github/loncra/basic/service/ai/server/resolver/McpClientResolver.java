package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientGroupEnum;

public interface McpClientResolver {

    McpClientWrapper getMcpClient();

    McpClientGroupEnum getGroup();

    boolean isRequired();

}
