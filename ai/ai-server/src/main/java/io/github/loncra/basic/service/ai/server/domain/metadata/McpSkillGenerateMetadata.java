package io.github.loncra.basic.service.ai.server.domain.metadata;

import io.github.loncra.basic.service.ai.api.domain.metadata.IdPluginMetadata;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class McpSkillGenerateMetadata extends IdPluginMetadata {

    @Serial
    private static final long serialVersionUID = -6306101341890721119L;

    private Instant creationTime;

    private List<McpSchema.Tool> tools = new LinkedList<>();
}
