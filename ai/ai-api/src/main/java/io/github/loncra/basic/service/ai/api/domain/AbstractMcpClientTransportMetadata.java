package io.github.loncra.basic.service.ai.api.domain;

import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class AbstractMcpClientTransportMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 5768987093562627974L;

    private Map<String, List<IdValueMetadata<String, String>>> failureAssertions = new LinkedHashMap<>();

    abstract public String getType();
}
