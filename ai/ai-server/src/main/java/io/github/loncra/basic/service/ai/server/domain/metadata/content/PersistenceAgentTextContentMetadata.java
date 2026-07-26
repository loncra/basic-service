package io.github.loncra.basic.service.ai.server.domain.metadata.content;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersistenceAgentTextContentMetadata extends AgentTextContentMetadata {

    @Serial
    private static final long serialVersionUID = 3572213447112357244L;

    private Instant creationTime;

    private Instant endTime;
}
