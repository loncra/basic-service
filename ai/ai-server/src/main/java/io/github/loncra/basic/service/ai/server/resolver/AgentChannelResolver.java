package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.BasicAgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentConversationTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

public interface AgentChannelResolver {

    ExecutionChannelEnum getChannel();

    static AgentChannelResolver channelResolver(
            List<AgentChannelResolver> resolvers,
            ExecutionChannelEnum channel
    ) {
        Objects.requireNonNull(channel, "执行通道不能为空");
        return resolvers.stream()
                .filter(s -> s.getChannel().equals(channel))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到通道 [" + channel + "] 的解析器"));
    }

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

    AgentExecutionMetadata getAgentExecutionMetadata(
            AgentExecuteMessageMetadata message,
            AuditAuthenticationToken token
    );

    List<AgentConversationEntity> findConversations(
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    AgentConversationEntity saveWorkspace(
            IdNameMetadata body,
            AuditAuthenticationToken token,
            String deviceIdentified,
            AgentConversationTypeEnum type
    );

    HarnessAgent createHarnessAgent(AgentExecutionMetadata execution);

    AgentStateStore openStateStore(BasicAgentExecutionMetadata execution);

    AgentRecordStore getAgentRecordStore();

    void deleteConversation(
            String id,
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    default void deleteConversation(
            List<String> ids,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        ids.forEach(id -> deleteConversation(id, token, deviceIdentified));
    }

    Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            String conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    );
}
