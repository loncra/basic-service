package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentTokenUsageMetadata extends AbstractAssistantMessageContentMetadata {

    @Serial
    private static final long serialVersionUID = 1L;

    private int inputTokens;
    private int outputTokens;
    private int cachedTokens;

    private AgentMessageContentTypeEnum usageType;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOKEN_USAGE.getValue();
    }
}
