package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockDeltaContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ThinkBlockContentMetadata extends AbstractBlockDeltaContentMetadata {

    @Serial
    private static final long serialVersionUID = 7549293785611608629L;

    private ToolCallBlockContentMetadata toolCall;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.THINK.getValue();
    }
}
