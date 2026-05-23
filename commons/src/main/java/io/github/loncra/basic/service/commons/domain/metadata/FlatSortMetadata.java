package io.github.loncra.basic.service.commons.domain.metadata;

import io.github.loncra.framework.commons.id.BasicIdentification;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FlatSortMetadata<T> implements BasicIdentification<T> {

    @NotNull
    private T id;

    @NotNull
    private Integer sort;
}
