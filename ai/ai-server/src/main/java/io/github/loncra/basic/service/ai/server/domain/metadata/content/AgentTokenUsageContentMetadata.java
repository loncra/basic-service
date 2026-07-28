package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentTokenUsageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentTokenUsageContentMetadata extends AgentAssistantMessageContent {

    @Serial
    private static final long serialVersionUID = 1L;

    private int inputTokens;
    private int outputTokens;
    private int cachedTokens;

    private AgentTokenUsageTypeEnum usageType;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOKEN_USAGE.getValue();
    }
}
