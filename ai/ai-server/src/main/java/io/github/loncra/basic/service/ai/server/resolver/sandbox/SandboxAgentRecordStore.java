package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.model.LsResult;
import io.agentscope.harness.agent.filesystem.model.ReadResult;
import io.agentscope.harness.agent.filesystem.model.WriteResult;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class SandboxAgentRecordStore implements AgentRecordStore {

    private static final String EMPTY_FILE = "System reminder: File exists but has empty contents";

    private final Function<String, AbstractSandboxFilesystem> records;

    private final Supplier<String> home;

    public SandboxAgentRecordStore(Function<String, AbstractSandboxFilesystem> records, Supplier<String> home) {
        this.records = records;
        this.home = home;
    }

    @Override
    public void updateConversation(AgentConversationEntity conversation, String deviceIdentified) {
        writeText(
                records.apply(deviceIdentified),
                AgentSandboxLayout.conversationFile(home.get(), conversation.getPrincipal(), conversation.getId()),
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
                readText(records.apply(deviceIdentified), AgentSandboxLayout.conversationFile(home.get(), principal, conversationId)),
                AgentConversationEntity.class
        );
    }

    @Override
    public void saveMessage(AgentMessageEntity message, String deviceIdentified) {
        String principal = message.getPrincipal();
        AbstractSandboxFilesystem store = records.apply(deviceIdentified);
        writeText(store, AgentSandboxLayout.messageFile(home.get(), principal, message.getId()), toJson(message));
        List<String> messageIds = readMessageIds(store, principal, message.getAgentConversationId());
        if (!messageIds.contains(message.getId())) {
            messageIds.add(message.getId());
            writeText(
                    store,
                    AgentSandboxLayout.messageIndexFile(home.get(), principal, message.getAgentConversationId()),
                    toJson(messageIds)
            );
        }
    }

    @Override
    public AgentMessageEntity getMessage(String principal, String messageId, String deviceIdentified) {
        return fromJson(
                readText(records.apply(deviceIdentified), AgentSandboxLayout.messageFile(home.get(), principal, messageId)),
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
        AbstractSandboxFilesystem store = records.apply(deviceIdentified);
        return listNames(store, AgentSandboxLayout.conversationsDir(home.get(), principal)).stream()
                .filter(name -> name.endsWith(AgentSandboxLayout.JSON_SUFFIX))
                .map(name -> name.substring(0, name.length() - AgentSandboxLayout.JSON_SUFFIX.length()))
                .map(id -> fromJson(readText(store, AgentSandboxLayout.conversationFile(home.get(), principal, id)), AgentConversationEntity.class))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteConversationFiles(AgentConversationEntity conversation, String deviceIdentified) {
        String principal = conversation.getPrincipal();
        AbstractSandboxFilesystem store = records.apply(deviceIdentified);
        for (String messageId : readMessageIds(store, principal, conversation.getId())) {
            deleteFile(store, AgentSandboxLayout.messageFile(home.get(), principal, messageId));
        }
        deleteFile(store, AgentSandboxLayout.messageIndexFile(home.get(), principal, conversation.getId()));
        deleteFile(store, AgentSandboxLayout.conversationFile(home.get(), principal, conversation.getId()));
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

    private List<String> readMessageIds(AbstractSandboxFilesystem store, String principal, String conversationId) {
        String json = readText(store, AgentSandboxLayout.messageIndexFile(home.get(), principal, conversationId));
        if (Objects.isNull(json) || json.isBlank()) {
            return new LinkedList<>();
        }
        List<?> raw = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, List.class));
        return raw.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedList::new));
    }

    private static void deleteFile(AbstractSandboxFilesystem filesystem, String path) {
        WriteResult deleted = filesystem.delete(RuntimeContext.empty(), path);
        if (!deleted.isSuccess()) {
            throw new SystemException("删除沙箱文件 [" + path + "] 失败: " + deleted.error());
        }
    }

    private static void writeText(AbstractSandboxFilesystem filesystem, String path, String content) {
        RuntimeContext context = RuntimeContext.empty();
        if (filesystem.exists(context, path)) {
            deleteFile(filesystem, path);
        }
        WriteResult written = filesystem.write(context, path, content);
        if (!written.isSuccess()) {
            throw new SystemException("写入沙箱文件 [" + path + "] 失败: " + written.error());
        }
    }

    private static String readText(AbstractSandboxFilesystem filesystem, String path) {
        ReadResult result = filesystem.read(RuntimeContext.empty(), path, 0, 0);
        if (!result.isSuccess() || result.fileData() == null || result.fileData().content() == null) {
            return null;
        }
        String content = result.fileData().content();
        if (content.isBlank() || content.startsWith(EMPTY_FILE)) {
            return "";
        }
        return content;
    }

    private static List<String> listNames(AbstractSandboxFilesystem filesystem, String directory) {
        LsResult result = filesystem.ls(RuntimeContext.empty(), directory);
        if (!result.isSuccess() || result.entries() == null) {
            return List.of();
        }
        return result.entries().stream()
                .map(entry -> {
                    String path = entry.path().replace('\\', '/');
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    int slash = path.lastIndexOf('/');
                    return slash < 0 ? path : path.substring(slash + 1);
                })
                .filter(name -> !name.isEmpty())
                .toList();
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
