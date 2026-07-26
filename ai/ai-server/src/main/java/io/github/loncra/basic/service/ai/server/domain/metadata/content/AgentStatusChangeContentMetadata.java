package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentStatusChangeContentMetadata extends AgentAssistantMessageContent {

    @Serial
    private static final long serialVersionUID = 5952053464326295520L;

    private AgentChatStatusEnum status;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.AGENT_STATUS_CHANGE.getValue();
    }
}
