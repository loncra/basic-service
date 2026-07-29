package io.github.loncra.basic.service.ai.server.domain.metadata.model;

import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class ModelResolverMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -7846961159494746105L;

    private Model model;

    private Toolkit toolkit;
}
