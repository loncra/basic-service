package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
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

    public static final String EVN_KEY = "env";

    public static final String ARGS_KEY = "args";

    @Serial
    private static final long serialVersionUID = 8125138388121138820L;

    private String command;

    private List<String> args = new LinkedList<>();

    private Map<String, String> env = new LinkedHashMap<>();

    @Override
    public String getType() {
        return McpClientTypeEnum.STDIO.toString();
    }

    @Override
    public void configDefaultValue(Map<String, Object> defaultConfig) {
        if (defaultConfig.containsKey(McpClientStdioTransportMetadata.EVN_KEY)) {
            List<IdValueMetadata<String, String>> values = CastUtils.convertValue(
                    defaultConfig.get(McpClientStdioTransportMetadata.EVN_KEY), new TypeReference<>() {
                    }
            );

            values.forEach(s -> env.put(s.getId(), s.getValue()));
        }

        if (defaultConfig.containsKey(McpClientStdioTransportMetadata.ARGS_KEY)) {
            List<IdValueMetadata<String, String>> values = CastUtils.convertValue(
                    defaultConfig.get(McpClientStdioTransportMetadata.ARGS_KEY), new TypeReference<>() {
                    }
            );

            values.forEach(s -> args.add( s.getId() + HttpRequestParameterMapUtils.EQ + s.getValue()));
        }
    }
}
