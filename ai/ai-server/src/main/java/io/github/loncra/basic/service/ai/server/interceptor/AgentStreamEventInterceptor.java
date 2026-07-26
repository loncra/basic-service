package io.github.loncra.basic.service.ai.server.interceptor;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;

import java.util.List;

public interface AgentStreamEventInterceptor {

    List<AgentAssistantMessageContent> postEventsStream(AgentMessageEntity assistant);
}
