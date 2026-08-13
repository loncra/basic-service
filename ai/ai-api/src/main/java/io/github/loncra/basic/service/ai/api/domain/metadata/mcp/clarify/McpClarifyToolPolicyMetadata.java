package io.github.loncra.basic.service.ai.api.domain.metadata.mcp.clarify;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class McpClarifyToolPolicyMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 6785236276279695359L;

    private YesOrNo enabled = YesOrNo.No;

    private Integer maxClarifyRounds = 3;

    private String toolName;
}
