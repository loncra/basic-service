package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.framework.commons.enumerate.basic.ExecuteStatus;

import java.util.List;

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
}
