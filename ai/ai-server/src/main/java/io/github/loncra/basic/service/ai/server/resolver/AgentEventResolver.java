package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;

/**
 * AgentScope 事件解析器：按 isSupport 命中后 process。
 */
public interface AgentEventResolver {

    boolean isSupport(AgentEvent event);

    AgentAssistantMessageContent process(AgentMessageEntity assistant, AgentEvent event, RuntimeContext context);
}
