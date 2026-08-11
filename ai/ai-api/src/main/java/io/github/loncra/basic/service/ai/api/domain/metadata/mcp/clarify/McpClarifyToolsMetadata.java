package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.clarify;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
public class McpClarifyToolsMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -8171997325200327543L;

    private YesOrNo enabled = YesOrNo.No;

    private List<McpClarifyToolPolicyMetadata> policies = new LinkedList<>();
}
