package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.github.loncra.basic.service.ai.server.constants.AgentAnswerConstants;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockDeltaContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnswerBlockContentMetadata extends AbstractBlockDeltaContentMetadata {
    @Serial
    private static final long serialVersionUID = -2958030676489506997L;

    /**
     * {@link AgentAnswerConstants#FORMAT_MARKDOWN} 或 {@link AgentAnswerConstants#FORMAT_A2UI}
     */
    private String format = AgentAnswerConstants.FORMAT_MARKDOWN;

    /** 触发合成的 exit 工具名，如 clarify_exit / plan_exit */
    private String sourceExit;

    private String hitlToolCallId;

    /** A2UI v0.9 commands；仅 format=a2ui 时有值 */
    private List<Map<String, Object>> commands = new LinkedList<>();

    private String surfaceId;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.ANSWER.getValue();
    }
}
