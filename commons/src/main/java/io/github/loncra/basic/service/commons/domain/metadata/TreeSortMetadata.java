package io.github.loncra.basic.service.commons.domain.metadata;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 树形排序元数据信息
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TreeSortMetadata<T> extends FlatSortMetadata<T> {

    private T parentId;
}
