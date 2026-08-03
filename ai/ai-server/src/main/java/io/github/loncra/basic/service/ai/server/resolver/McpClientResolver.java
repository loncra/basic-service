package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.server.enumerate.ToolGroupEnum;

public interface McpClientResolver {

    McpClientWrapper getClient();

    ToolGroupEnum getGroup();

    boolean isRequired();

}
