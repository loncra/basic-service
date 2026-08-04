package io.github.loncra.basic.service.ai.server.domain.metadata.mcp;

import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientTypeEnum;
import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class McpClientSseTransportMetadata extends AbstractMcpClientTransportMetadata  {

    @Serial
    private static final long serialVersionUID = 8125138388121138820L;

    private String baseUrl;

    private String endpoint;

    private TimeProperties timeout;

    private Map<String, List<String>> headers = new LinkedHashMap<>();

    private Map<String, List<String>> queryParams = new LinkedHashMap<>();

    @Override
    public String getType() {
        return McpClientTypeEnum.SSE.toString();
    }
}
