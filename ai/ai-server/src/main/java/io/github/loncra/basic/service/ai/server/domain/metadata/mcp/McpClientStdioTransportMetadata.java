package io.github.loncra.basic.service.ai.server.domain.metadata.mcp;

import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientTypeEnum;

import java.io.Serial;

public class McpClientStdioTransportMetadata extends AbstractMcpClientTransportMetadata  {

    @Serial
    private static final long serialVersionUID = 8125138388121138820L;

    @Override
    public String getType() {
        return McpClientTypeEnum.STDIO.toString();
    }
}
