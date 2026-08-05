package io.github.loncra.basic.service.ai.server.domain.metadata;

import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.framework.commons.id.StringIdEntity;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SkillMetadata extends StringIdEntity {

    @Serial
    private static final long serialVersionUID = -6306101341890721119L;

    private String description;

    private DataDictionaryMetadata group;

    private List<String> tags = new LinkedList<>();

    private List<McpSchema.Tool> tools = new LinkedList<>();
}
