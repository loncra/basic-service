package io.github.loncra.basic.service.ai.server.domain.metadata.agent;

import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class AgentExecuteMessageMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ExecutionChannelEnum channel;

    private String ref;

    private String deviceIdentified;
}
