package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.api.enumerate.AgentToolCallStatusEnum;
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
public class AgentToolCallStartContentMetadata extends AgentAssistantMessageContent {

    @Serial
    private static final long serialVersionUID = -8972671412121985051L;

    private Instant creationTime;

    private String name;

    private Object input;

    private AgentToolCallStatusEnum status;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOOL_START.getValue();
    }
}
