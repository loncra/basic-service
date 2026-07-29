package io.github.loncra.basic.service.ai.server.domain.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBlockDeltaContentMetadata extends AbstractBlockRunningContentMetadata {

    @Serial
    private static final long serialVersionUID = 3184726509182736451L;

    private String value;
}
