package io.github.loncra.basic.service.ai.server.domain.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract class AbstractMcpClientTransportMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 5768987093562627974L;

    abstract public String getType();
}
