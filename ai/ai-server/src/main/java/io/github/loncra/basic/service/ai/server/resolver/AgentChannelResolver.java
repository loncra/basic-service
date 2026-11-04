package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;

public interface AgentChannelResolver {

    ExecutionChannelEnum channel();

    AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    HarnessAgent createHarnessAgent(AgentMessageEntity assistant);

    AbstractFilesystem createFilesystem(AgentMessageEntity assistant, String workspacePath);
}
