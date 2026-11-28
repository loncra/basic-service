package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.auth.api.enumerate.TerminalTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.OwnershipEnum;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;

import java.util.List;

public interface AgentChannelResolver {

    String WORKSPACE_KIND_METADATA_KEY = "kind";

    String WORKSPACE_KEY_METADATA_KEY = "key";

    String WORKSPACE_ROOTS_METADATA_KEY = "roots";

    ExecutionChannelEnum getChannel();

    List<TerminalTypeEnum> getSupportTerminalTypes();

    AgentChatResponseBody chat(
            AgentChatRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    AgentChatBasicResponseBody resume(
            AgentResumeRequestBody body,
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    AgentConversationEntity saveWorkspace(
            IdNameMetadata body,
            AuditAuthenticationToken token,
            String deviceIdentified,
            AgentConversationTypeEnum type,
            OwnershipEnum ownership
    );

    List<AgentConversationEntity> findConversations(
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    default List<AgentConversationEntity> findWorkspaces(
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        List<AgentConversationEntity> list = getAgentRecordStore().listConversations(token.getName(), deviceIdentified);

        return list.stream()
                .filter(s -> !AgentConversationTypeEnum.WORKSPACE_CONVERSATION.equals(s.getType()))
                .toList();
    }

    HarnessAgent createHarnessAgent(AgentExecutionMetadata execution);

    AgentStateStore getAgentStateStore(BasicAgentExecutionMetadata execution);

    AgentRecordStore getAgentRecordStore();
}
