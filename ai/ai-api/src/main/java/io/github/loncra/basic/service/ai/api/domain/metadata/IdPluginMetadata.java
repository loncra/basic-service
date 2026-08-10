package io.github.loncra.basic.service.ai.api.domain.metadata;

import io.github.loncra.basic.service.ai.api.domain.BasicPluginMetadata;
import io.github.loncra.framework.commons.id.BasicIdentification;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IdPluginMetadata extends BasicPluginMetadata implements BasicIdentification<String> {

    @Serial
    private static final long serialVersionUID = -4428832092357302504L;

    private String id;
}
