package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.ToolCallState;
import io.agentscope.core.message.ToolResultState;
import io.agentscope.core.message.ToolUseBlock;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockDeltaContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolCallBlockContentMetadata extends AbstractBlockDeltaContentMetadata {

    @Serial
    private static final long serialVersionUID = -4568583911618929598L;

    private String groupId;

    private String name;

    private List<ContentBlock> outputParts;

    private String outputText;

    private ToolResultState resultState;

    private ToolCallState hitlStatus;

    private Boolean userConfirmed;

    @Override
    public String getType() {
        return AgentMessageContentTypeEnum.TOOL_CALL.getValue();
    }

    public ToolUseBlock toToolUseBlock() {

        Map<String, Object> input = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(getValue(), CastUtils.MAP_TYPE_REFERENCE));
        return ToolUseBlock.builder()
                .name(name)
                .id(getId())
                .input(input)
                .state(hitlStatus)
                .build();
    }
}
