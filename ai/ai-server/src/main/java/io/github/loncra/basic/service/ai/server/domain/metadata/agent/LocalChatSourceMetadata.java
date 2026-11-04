package io.github.loncra.basic.service.ai.server.domain.metadata.agent;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class LocalChatSourceMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String workspaceKey;

    private String workspaceName;

    private String conversationId;
}
