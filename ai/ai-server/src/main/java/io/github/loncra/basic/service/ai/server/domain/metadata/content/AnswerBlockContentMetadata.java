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
public class AnswerBlockContentMetadata extends AbstractBlockDeltaContentMetadata {
    @Serial
    private static final long serialVersionUID = -2958030676489506997L;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.ANSWER.getValue();
    }
}
