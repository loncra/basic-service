package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatBasicResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatRequestBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentChatResponseBody;
import io.github.loncra.basic.service.ai.server.domain.body.AgentResumeRequestBody;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecuteMessageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

public interface AgentChannelResolver {

    ExecutionChannelEnum channel();

    static AgentChannelResolver channelResolver(List<AgentChannelResolver> resolvers, ExecutionChannelEnum channel) {
        Objects.requireNonNull(channel, "执行通道不能为空");
        return resolvers.stream()
                .filter(s -> s.channel() == channel)
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

    AgentExecutionMetadata getAgentExecutionMetadata(AgentExecuteMessageMetadata message);

    HarnessAgent createHarnessAgent(AgentExecutionMetadata execution);

    AbstractFilesystem createFilesystem(AgentExecutionMetadata execution, String workspacePath);

    void saveConversation(AgentConversationEntity conversation, String deviceIdentified);

    AgentConversationEntity getConversation(Long conversationId, String deviceIdentified);

    void saveMessage(AgentMessageEntity message, String deviceIdentified);

    AgentMessageEntity getMessage(Long messageId, String deviceIdentified);

    List<AgentMessageEntity> getConversationMessages(Long conversationId, String deviceIdentified);

    List<AgentMessageEntity> findRequestStopAssistantMessage(Long conversationId, String deviceIdentified);

    void deleteConversationData(Long conversationId, String deviceIdentified);

    void deleteConversation(List<Integer> ids, AuditAuthenticationToken token, String deviceIdentified);

    Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            Long conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    );

    int positioningMessagePageNumber(Long conversationId, Long messageId, int pageSize, String deviceIdentified);

    void saveAssistant(AgentExecutionMetadata execution, AgentMessageEntity assistant);

    void saveConversationStatus(AgentExecutionMetadata execution, AgentChatStatusEnum status);
}
