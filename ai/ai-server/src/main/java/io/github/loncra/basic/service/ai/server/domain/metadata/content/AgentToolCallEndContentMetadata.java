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
public class AgentToolCallEndContentMetadata extends AgentAssistantMessageContent {

    @Serial
    private static final long serialVersionUID = 6052070035325088880L;

    private Instant endTime;

    private Object output;

    private String name;

    private AgentBlockStatusEnum status;

    private String resultState;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOOL_END.getValue();
    }
}
