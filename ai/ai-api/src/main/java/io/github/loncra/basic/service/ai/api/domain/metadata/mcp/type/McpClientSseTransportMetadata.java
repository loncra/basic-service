package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class McpClientSseTransportMetadata extends AbstractMcpClientTransportMetadata {

    public static final String QUERY_PARAMS_KEY = "queryParams";

    public static final String HEADERS_KEY = "headers";

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

    @Override
    public void configDefaultValue(Map<String, Object> defaultConfig) {
        if (defaultConfig.containsKey(McpClientSseTransportMetadata.HEADERS_KEY)) {
            List<IdValueMetadata<String, String>> values = CastUtils.convertValue(
                    defaultConfig.get(McpClientSseTransportMetadata.HEADERS_KEY), new TypeReference<>() {
                    }
            );

            values.forEach(s -> headers.computeIfAbsent(s.getId(), k -> new LinkedList<>()).add(s.getValue()));
        }


        if (defaultConfig.containsKey(McpClientSseTransportMetadata.QUERY_PARAMS_KEY)) {
            List<IdValueMetadata<String, String>> values = CastUtils.convertValue(
                    defaultConfig.get(McpClientSseTransportMetadata.QUERY_PARAMS_KEY), new TypeReference<>() {
                    }
            );

            values.forEach(s -> queryParams.computeIfAbsent(s.getId(), k -> new LinkedList<>()).add(s.getValue()));
        }
    }
}
