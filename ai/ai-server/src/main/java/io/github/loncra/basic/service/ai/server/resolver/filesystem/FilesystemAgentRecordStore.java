package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentRecordStore;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FilesystemAgentRecordStore implements AgentRecordStore {

    private final Function<String, AbstractFilesystem> records;

    public FilesystemAgentRecordStore(Function<String, AbstractFilesystem> records) {
        this.records = records;
    }

    @Override
    public void updateConversation(AgentConversationEntity conversation, String deviceIdentified) {
        FilesystemOptions.writeText(
                records.apply(deviceIdentified),
                FilesystemLayout.conversationFile(conversation.getPrincipal(), conversation.getId()),
                toJson(conversation)
        );
    }

    @Override
    public void insertConversation(AgentConversationEntity conversation, String deviceIdentified) {
        updateConversation(conversation, deviceIdentified);
    }

    @Override
    public AgentConversationEntity getConversation(String principal, String conversationId, String deviceIdentified) {
        return fromJson(
                FilesystemOptions.readText(records.apply(deviceIdentified), FilesystemLayout.conversationFile(principal, conversationId)),
                AgentConversationEntity.class
        );
    }

    @Override
    public void saveMessage(AgentMessageEntity message, String deviceIdentified) {
        String principal = message.getPrincipal();
        AbstractFilesystem store = records.apply(deviceIdentified);
        FilesystemOptions.writeText(store, FilesystemLayout.messageFile(principal, message.getId()), toJson(message));
        List<String> messageIds = readMessageIds(store, principal, message.getAgentConversationId());
        if (!messageIds.contains(message.getId())) {
            messageIds.add(message.getId());
            FilesystemOptions.writeText(
                    store,
                    FilesystemLayout.messageIndexFile(principal, message.getAgentConversationId()),
                    toJson(messageIds)
            );
        }
    }

    @Override
    public AgentMessageEntity getMessage(String principal, String messageId, String deviceIdentified) {
        return fromJson(
                FilesystemOptions.readText(records.apply(deviceIdentified), FilesystemLayout.messageFile(principal, messageId)),
                AgentMessageEntity.class
        );
    }

    @Override
    public List<AgentMessageEntity> getConversationMessages(String principal, String conversationId, String deviceIdentified) {
        return readMessageIds(records.apply(deviceIdentified), principal, conversationId).stream()
                .map(id -> getMessage(principal, id, deviceIdentified))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<AgentMessageEntity> findRequestStopAssistantMessage(String principal, String conversationId, String deviceIdentified) {
        return getConversationMessages(principal, conversationId, deviceIdentified).stream()
                .filter(item -> AgentChatStatusEnum.REQUEST_STOP.equals(item.getStatus()))
                .filter(item -> AgentMessageRoleEnum.ASSISTANT.equals(item.getRole()))
                .toList();
    }

    @Override
    public List<AgentConversationEntity> listConversations(String principal, String deviceIdentified) {
        AbstractFilesystem store = records.apply(deviceIdentified);
        return FilesystemOptions.listNames(store, FilesystemLayout.conversationsDir(principal)).stream()
                .filter(name -> name.endsWith(FilesystemLayout.JSON_SUFFIX))
                .map(name -> name.substring(0, name.length() - FilesystemLayout.JSON_SUFFIX.length()))
                .map(id -> fromJson(FilesystemOptions.readText(store, FilesystemLayout.conversationFile(principal, id)), AgentConversationEntity.class))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteConversationFiles(AgentConversationEntity conversation, String deviceIdentified) {
        String principal = conversation.getPrincipal();
        AbstractFilesystem store = records.apply(deviceIdentified);
        for (String messageId : readMessageIds(store, principal, conversation.getId())) {
            FilesystemOptions.deleteFile(store, FilesystemLayout.messageFile(principal, messageId));
        }
        FilesystemOptions.deleteFile(store, FilesystemLayout.messageIndexFile(principal, conversation.getId()));
        FilesystemOptions.deleteFile(store, FilesystemLayout.conversationFile(principal, conversation.getId()));
    }

    @Override
    public int positioningMessagePageNumber(String principal, String conversationId, String messageId, int pageSize, String deviceIdentified) {
        List<AgentMessageEntity> messages = getConversationMessages(principal, conversationId, deviceIdentified).stream()
                .sorted(Comparator.comparing(AgentMessageEntity::getId).reversed())
                .toList();
        int index = 0;
        for (AgentMessageEntity message : messages) {
            if (messageId.equals(message.getId())) {
                break;
            }
            index++;
        }
        return index >= messages.size() ? 1 : index / pageSize + 1;
    }

    @Override
    public void saveAssistant(AgentExecutionMetadata execution, AgentMessageEntity assistant) {
        saveMessage(assistant, execution.getDeviceIdentified());
    }

    @Override
    public void saveConversationStatus(AgentExecutionMetadata execution, AgentChatStatusEnum status) {
        AgentConversationEntity conversation = execution.getConversation();
        if (Objects.isNull(conversation)) {
            return;
        }
        conversation.setStatus(status);
        updateConversation(conversation, execution.getDeviceIdentified());
    }

    private List<String> readMessageIds(AbstractFilesystem store, String principal, String conversationId) {
        String json = FilesystemOptions.readText(store, FilesystemLayout.messageIndexFile(principal, conversationId));
        if (Objects.isNull(json) || json.isBlank()) {
            return new LinkedList<>();
        }
        List<?> raw = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, List.class));
        return raw.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedList::new));
    }

    private String toJson(Object value) {
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(value));
    }

    private <T> T fromJson(String json, Class<T> type) {
        if (Objects.isNull(json) || json.isBlank()) {
            return null;
        }
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, type));
    }
}
