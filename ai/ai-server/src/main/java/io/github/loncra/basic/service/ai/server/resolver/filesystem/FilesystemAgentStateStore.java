package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.WriteResult;
import io.github.loncra.framework.commons.exception.SystemException;

import java.util.*;

public class FilesystemAgentStateStore implements AgentStateStore {

    public static final String AGENT_STATE_KEY = "agent_state";

    private static final String ANON_USER = "__anon__";

    private final AbstractFilesystem filesystem;

    public FilesystemAgentStateStore(AbstractFilesystem filesystem) {
        this.filesystem = filesystem;
    }

    @Override
    public void save(String userId, String sessionId, String key, State value) {
        String json = JsonUtils.getJsonCodec().toPrettyJson(value);
        FilesystemOptions.writeText(filesystem, FilesystemLayout.stateFile(owner(userId), sessionId, key), json);
    }

    @Override
    public void save(String userId, String sessionId, String key, List<? extends State> values) {
        String content = values.stream()
                .map(item -> JsonUtils.getJsonCodec().toJson(item))
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");
        FilesystemOptions.writeText(filesystem, FilesystemLayout.stateListFile(owner(userId), sessionId, key), content);
    }

    @Override
    public <T extends State> Optional<T> get(String userId, String sessionId, String key, Class<T> type) {
        String json = FilesystemOptions.readText(filesystem, FilesystemLayout.stateFile(owner(userId), sessionId, key));
        if (Objects.isNull(json) || json.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(SystemException.convertSupplier(() -> JsonUtils.getJsonCodec().fromJson(json, type)));
    }

    @Override
    public <T extends State> List<T> getList(String userId, String sessionId, String key, Class<T> itemType) {
        String content = FilesystemOptions.readText(filesystem, FilesystemLayout.stateListFile(owner(userId), sessionId, key));
        if (Objects.isNull(content) || content.isBlank()) {
            return List.of();
        }
        return content.lines()
                .filter(line -> !line.isBlank())
                .map(line -> SystemException.convertSupplier(() -> JsonUtils.getJsonCodec().fromJson(line, itemType)))
                .toList();
    }

    @Override
    public boolean exists(String userId, String sessionId) {
        return filesystem.exists(RuntimeContext.empty(), FilesystemLayout.stateSession(owner(userId), sessionId));
    }

    @Override
    public void delete(String userId, String sessionId) {
        WriteResult deleted = filesystem.delete(RuntimeContext.empty(), FilesystemLayout.stateSession(owner(userId), sessionId));
        if (!deleted.isSuccess()) {
            throw new SystemException("删除沙箱目录 [" + FilesystemLayout.stateSession(owner(userId), sessionId) + "] 失败: " + deleted.error());
        }
    }

    @Override
    public void delete(String userId, String sessionId, String key) {
        FilesystemOptions.deleteFile(filesystem, FilesystemLayout.stateFile(owner(userId), sessionId, key));
        FilesystemOptions.deleteFile(filesystem, FilesystemLayout.stateListFile(owner(userId), sessionId, key));
    }

    @Override
    public Set<String> listSessionIds(String userId) {
        return new LinkedHashSet<>(FilesystemOptions.listNames(filesystem, FilesystemLayout.stateDir(owner(userId))));
    }

    private static String owner(String userId) {
        return Objects.isNull(userId) || userId.isBlank() ? ANON_USER : userId;
    }
}
