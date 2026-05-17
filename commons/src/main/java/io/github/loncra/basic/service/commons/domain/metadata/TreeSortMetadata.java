package io.github.loncra.basic.service.commons.domain.metadata;

import io.github.loncra.framework.commons.id.BasicIdentification;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 树形排序元数据信息
 *
 * @author maurice.chen
 */
@Data
public class TreeSortMetadata<T> implements BasicIdentification<T> {

    @NotNull
    private T id;

    private T parentId;

    @NotNull
    private Integer sort;
}
