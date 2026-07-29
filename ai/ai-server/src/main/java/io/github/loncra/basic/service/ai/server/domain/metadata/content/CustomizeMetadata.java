package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CustomizeMetadata extends AbstractAssistantMessageContentMetadata {

    @Serial
    private static final long serialVersionUID = 780885664106994887L;

    /**
     * 块语义
     */
    private AgentMessageContentTypeEnum eventType;

    private Map<String, Object> metadata = new LinkedHashMap<>();

    @Override
    public String getType() {
        return eventType.getValue();
    }
}
