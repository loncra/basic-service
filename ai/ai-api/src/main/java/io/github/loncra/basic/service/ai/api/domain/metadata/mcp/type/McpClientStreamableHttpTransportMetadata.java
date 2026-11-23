package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class McpClientStreamableHttpTransportMetadata extends McpClientSseTransportMetadata  {


    public static final String BODY_KEY = "body";

    @Serial
    private static final long serialVersionUID = 8125138388121138820L;

    private boolean openConnectionOnStartup;

    private boolean resumableStreams;

    private Map<String, Object> body;

    @Override
    public String getType() {
        return McpClientTypeEnum.STREAMABLE_HTTP.toString();
    }

    @Override
    public void configDefaultValue(Map<String, Object> defaultConfig) {
        super.configDefaultValue(defaultConfig);

        if (defaultConfig.containsKey(McpClientStreamableHttpTransportMetadata.BODY_KEY)) {
            List<IdValueMetadata<String, String>> values = CastUtils.convertValue(
                    defaultConfig.get(McpClientStreamableHttpTransportMetadata.BODY_KEY), new TypeReference<>() {
                    }
            );

            values.forEach(s -> body.put(s.getId(), s.getValue()));
        }
    }
}
