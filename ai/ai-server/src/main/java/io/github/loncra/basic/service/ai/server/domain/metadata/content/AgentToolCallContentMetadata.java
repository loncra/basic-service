package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentToolCallContentMetadata extends RunningContentMetadata {

    @Serial
    private static final long serialVersionUID = -4568583911618929598L;

    private String name;

    private Object input;

    private Object output;

    private String resultState;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOOL.getValue();
    }
}
