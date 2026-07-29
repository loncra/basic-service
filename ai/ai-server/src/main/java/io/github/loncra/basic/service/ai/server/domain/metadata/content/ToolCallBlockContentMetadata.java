package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.agentscope.core.message.ToolResultState;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockDeltaContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolCallBlockContentMetadata extends AbstractBlockDeltaContentMetadata {

    @Serial
    private static final long serialVersionUID = -4568583911618929598L;

    private String name;

    private Object output;

    private ToolResultState resultState;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOOL_CALL.getValue();
    }
}
