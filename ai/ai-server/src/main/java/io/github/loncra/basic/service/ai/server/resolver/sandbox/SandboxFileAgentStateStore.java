package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import io.agentscope.harness.agent.filesystem.model.WriteResult;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.github.loncra.framework.commons.exception.SystemException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class SandboxFileAgentStateStore implements AgentStateStore {

    public static final String AGENT_STATE_KEY = "agent_state";

    private static final String ANON_USER = "__anon__";

    private final AbstractSandboxFilesystem filesystem;

    private final String home;

    public SandboxFileAgentStateStore(AbstractSandboxFilesystem filesystem, String home) {
        this.filesystem = filesystem;
        this.home = home;
    }

    @Override
    public void save(String userId, String sessionId, String key, State value) {
        String json = JsonUtils.getJsonCodec().toPrettyJson(value);
        SandboxFileOptions.writeText(filesystem, AgentSandboxLayout.stateFile(home, owner(userId), sessionId, key), json);
    }

    @Override
    public void save(String userId, String sessionId, String key, List<? extends State> values) {
        String content = values.stream()
                .map(item -> JsonUtils.getJsonCodec().toJson(item))
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");
        SandboxFileOptions.writeText(filesystem, AgentSandboxLayout.stateListFile(home, owner(userId), sessionId, key), content);
    }

    @Override
    public <T extends State> Optional<T> get(String userId, String sessionId, String key, Class<T> type) {
        String json = SandboxFileOptions.readText(filesystem, AgentSandboxLayout.stateFile(home, owner(userId), sessionId, key));
        if (Objects.isNull(json) || json.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(SystemException.convertSupplier(() -> JsonUtils.getJsonCodec().fromJson(json, type)));
    }

    @Override
    public <T extends State> List<T> getList(String userId, String sessionId, String key, Class<T> itemType) {
        String content = SandboxFileOptions.readText(filesystem, AgentSandboxLayout.stateListFile(home, owner(userId), sessionId, key));
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
        return filesystem.exists(RuntimeContext.empty(), AgentSandboxLayout.stateSession(home, owner(userId), sessionId));
    }

    @Override
    public void delete(String userId, String sessionId) {
        WriteResult deleted = filesystem.delete(RuntimeContext.empty(), AgentSandboxLayout.stateSession(home, owner(userId), sessionId));
        if (!deleted.isSuccess()) {
            throw new SystemException("删除沙箱目录 [" + AgentSandboxLayout.stateSession(home, owner(userId), sessionId) + "] 失败: " + deleted.error());
        }
    }

    @Override
    public void delete(String userId, String sessionId, String key) {
        SandboxFileOptions.deleteFile(filesystem, AgentSandboxLayout.stateFile(home, owner(userId), sessionId, key));
        SandboxFileOptions.deleteFile(filesystem, AgentSandboxLayout.stateListFile(home, owner(userId), sessionId, key));
    }

    @Override
    public Set<String> listSessionIds(String userId) {
        return new LinkedHashSet<>(SandboxFileOptions.listNames(filesystem, AgentSandboxLayout.stateDir(home, owner(userId))));
    }

    private static String owner(String userId) {
        return Objects.isNull(userId) || userId.isBlank() ? ANON_USER : userId;
    }
}
