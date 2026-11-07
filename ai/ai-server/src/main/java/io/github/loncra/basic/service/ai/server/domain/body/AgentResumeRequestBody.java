package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
public class AgentResumeRequestBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 5184078388976327968L;

    @NotNull
    private String assistantMessageId;

    @NotEmpty
    @NotNull
    private List<ConfirmResult> confirmResults = new LinkedList<>();

    @NotNull
    private ExecutionChannelEnum channel;

    @Data
    public static class ConfirmResult implements Serializable {
        @Serial
        private static final long serialVersionUID = -2203253161762634470L;

        private String toolCallId;

        private boolean confirmed;
    }
}
