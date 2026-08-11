package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class McpClientStdioTransportMetadata extends AbstractMcpClientTransportMetadata {

    private String command;

    private List<String> args = new LinkedList<>();

    private Map<String, String> env = new LinkedHashMap<>();

    @Serial
    private static final long serialVersionUID = 8125138388121138820L;

    @Override
    public String getType() {
        return McpClientTypeEnum.STDIO.toString();
    }
}
