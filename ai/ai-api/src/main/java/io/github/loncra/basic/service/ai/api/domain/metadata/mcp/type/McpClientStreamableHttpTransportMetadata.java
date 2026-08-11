package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type;

import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class McpClientStreamableHttpTransportMetadata extends McpClientSseTransportMetadata  {

    @Serial
    private static final long serialVersionUID = 8125138388121138820L;

    private boolean openConnectionOnStartup;

    private boolean resumableStreams;

    private Map<String, Object> body;

    @Override
    public String getType() {
        return McpClientTypeEnum.STREAMABLE_HTTP.toString();
    }
}
