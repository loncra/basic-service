package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AgentChatRequestBody extends AgentChatMetadata {

    @Serial
    private static final long serialVersionUID = 7988680530512203651L;
    /**
     * 工作空间 id
     */
    @NotNull
    private Long agentWorkspaceId;

    /**
     * 调用模型设置
     */
    @NotNull
    private Long modelId;

}
