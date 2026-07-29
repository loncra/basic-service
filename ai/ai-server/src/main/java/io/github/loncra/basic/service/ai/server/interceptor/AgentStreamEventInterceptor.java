package io.github.loncra.basic.service.ai.server.interceptor;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;

import java.util.List;

public interface AgentStreamEventInterceptor {

    List<AbstractAssistantMessageContentMetadata> postEventsStream(AgentMessageEntity assistant);
}
