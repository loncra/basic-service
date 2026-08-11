package io.github.loncra.basic.service.ai.api.domain.metadata;

import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.clarify.McpClarifyToolsMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClarifyPluginMetadata extends IdPluginMetadata {

    @Serial
    private static final long serialVersionUID = -4428832092357302504L;

    private McpClarifyToolsMetadata clarifyTools;
}
