package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.TotalPage;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface AgentRecordStore {

    void updateConversation(AgentConversationEntity conversation, String deviceIdentified);

    void insertConversation(AgentConversationEntity conversation, String deviceIdentified);

    AgentConversationEntity getConversation(String principal, String conversationId, String deviceIdentified);

    void saveMessage(AgentMessageEntity message, String deviceIdentified);

    AgentMessageEntity getMessage(String principal, String messageId, String deviceIdentified);

    List<AgentMessageEntity> getConversationMessages(String principal, String conversationId, String deviceIdentified);

    List<AgentMessageEntity> findRequestStopAssistantMessage(String principal, String conversationId, String deviceIdentified);

    List<AgentConversationEntity> listConversations(String principal, String deviceIdentified);

    void deleteConversationFiles(AgentConversationEntity conversation, String deviceIdentified);

    int positioningMessagePageNumber(String principal, String conversationId, String messageId, int pageSize, String deviceIdentified);

    void saveAssistant(AgentExecutionMetadata execution, AgentMessageEntity assistant);

    void saveConversationStatus(AgentExecutionMetadata execution, AgentChatStatusEnum status);

    default void updateConversationGenerateNameStatus(
            String id,
            ExecuteStatus status,
            AgentExecutionMetadata executionMetadata
    ) {
        AgentConversationEntity entity = getConversation(executionMetadata.getToken().getName(), id, executionMetadata.getDeviceIdentified());
        entity.setGenerateNameStatus(status);
        updateConversation(entity, executionMetadata.getDeviceIdentified());
    }

    default void updateAssistantMetadata(AgentMessageEntity assistantEntity, AgentExecutionMetadata executionMetadata) {
        saveMessage(assistantEntity, executionMetadata.getDeviceIdentified());
    }

    default void updateGenerateName(AgentConversationEntity conversation, AgentExecutionMetadata executionMetadata) {
        updateConversation(conversation, executionMetadata.getDeviceIdentified());
    }

    default Page<AgentMessageEntity> histories(
            PageRequest request,
            MultiValueMap<String, Object> filter,
            String conversationId,
            boolean totalPage,
            AuditAuthenticationToken token,
            String deviceIdentified
    ) {
        String principal = token.getName();
        AgentConversationEntity conversation = Objects.requireNonNull(
                getConversation(principal, conversationId, deviceIdentified),
                "找不到 ID 为 [" + conversationId + "] 的会话内容"
        );
        PrincipalDetailsConstants.equals(conversation, token);
        List<AgentMessageEntity> messages = getConversationMessages(principal, conversationId, deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentMessageEntity::getId).reversed())
                .toList();
        List<AgentMessageEntity> elements = messages.subList(
                Math.min(request.getOffset(), messages.size()),
                Math.min(request.getOffset() + request.getSize(), messages.size())
        );
        return totalPage ? new TotalPage<>(request, elements, messages.size()) : new Page<>(request, elements);
    }
}
