package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentToolCallContentMetadata extends AgentAssistantMessageContent {

    @Serial
    private static final long serialVersionUID = -4568583911618929598L;

    private Instant creationTime;

    private Instant endTime;

    private String name;

    private Object input;

    private Object output;

    private AgentBlockStatusEnum status;

    private String resultState;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOOL_START.getValue();
    }
}
