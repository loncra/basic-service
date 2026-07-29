package io.github.loncra.basic.service.ai.server.domain.metadata;

import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractBlockRunningContentMetadata extends AbstractAssistantMessageContentMetadata {

    @Serial
    private static final long serialVersionUID = -3307675767818133614L;

    private AgentBlockStatusEnum status;

    private Instant creationTime;

    private Instant endTime;

}
