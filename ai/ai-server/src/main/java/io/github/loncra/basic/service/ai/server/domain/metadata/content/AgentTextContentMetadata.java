package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.Objects;

/**
 * 文本类助手块（think / answer / error），以 {@link #eventType} 区分语义。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentTextContentMetadata extends AgentAssistantMessageContent {

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

    public static AgentTextContentMetadata of(AgentMessageContentTypeEnum type, String id, String value) {
        if (Objects.isNull(type) || !AgentMessageContentTypeEnum.TEXT_BLOCK_TYPE.contains(type)) {
            throw new SystemException(AgentTextContentMetadata.class.getSimpleName() + ".type 仅支持 " + AgentMessageContentTypeEnum.TEXT_BLOCK_TYPE + "，实际: " + type);
        }

        AgentTextContentMetadata block = new AgentTextContentMetadata();
        block.setEventType(type);
        block.setId(id);
        block.setValue(value);
        return block;
    }
}
