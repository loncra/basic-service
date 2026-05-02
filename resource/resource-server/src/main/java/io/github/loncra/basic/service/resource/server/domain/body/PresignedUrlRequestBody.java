package io.github.loncra.basic.service.resource.server.domain.body;

import io.github.loncra.framework.commons.TimeProperties;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author maurice.chen
 */
@Data
public class PresignedUrlRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 3057737273504620097L;

    private String method;

    private TimeProperties timeProperties;
}
