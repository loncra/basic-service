package io.github.loncra.basic.service.commons.domain.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutputStreamMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -8301906180131042391L;

    private byte[] bytes;

    private long size;
}
