package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 文本类助手块（think / answer / error），以 {@link #eventType} 区分语义。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentTextContentMetadata extends RunningContentMetadata {

    @Serial
    private static final long serialVersionUID = 3184726509182736451L;

    private String value;

    /**
     * 块语义：仅允许 think / answer / error。
     */
    private AgentMessageContentTypeEnum eventType;

    @Override
    public String getType() {
        return eventType.getValue();
    }
}
