package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import io.agentscope.harness.agent.filesystem.model.LsResult;
import io.agentscope.harness.agent.filesystem.model.ReadResult;
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

    private static final String EMPTY_FILE = "System reminder: File exists but has empty contents";

    private final AbstractSandboxFilesystem filesystem;

    private final String home;

    public SandboxFileAgentStateStore(AbstractSandboxFilesystem filesystem, String home) {
        this.filesystem = filesystem;
        this.home = home;
    }

    @Override
    public void save(String userId, String sessionId, String key, State value) {
        String json = JsonUtils.getJsonCodec().toPrettyJson(value);
        writeText(AgentSandboxLayout.stateFile(home, owner(userId), sessionId, key), json);
    }

    @Override
    public void save(String userId, String sessionId, String key, List<? extends State> values) {
        // ponytail: 每次整文件重写。列表变大后再改成按行追加。
        String content = values.stream()
                .map(item -> JsonUtils.getJsonCodec().toJson(item))
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");
        writeText(AgentSandboxLayout.stateListFile(home, owner(userId), sessionId, key), content);
    }

    @Override
    public <T extends State> Optional<T> get(String userId, String sessionId, String key, Class<T> type) {
        String json = readText(AgentSandboxLayout.stateFile(home, owner(userId), sessionId, key));
        if (Objects.isNull(json) || json.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(SystemException.convertSupplier(() -> JsonUtils.getJsonCodec().fromJson(json, type)));
    }

    @Override
    public <T extends State> List<T> getList(String userId, String sessionId, String key, Class<T> itemType) {
        String content = readText(AgentSandboxLayout.stateListFile(home, owner(userId), sessionId, key));
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
        deleteFile(AgentSandboxLayout.stateFile(home, owner(userId), sessionId, key));
        deleteFile(AgentSandboxLayout.stateListFile(home, owner(userId), sessionId, key));
    }

    @Override
    public Set<String> listSessionIds(String userId) {
        return new LinkedHashSet<>(listNames(AgentSandboxLayout.stateDir(home, owner(userId))));
    }

    private static String owner(String userId) {
        return Objects.isNull(userId) || userId.isBlank() ? ANON_USER : userId;
    }

    private void writeText(String path, String content) {
        RuntimeContext context = RuntimeContext.empty();
        if (filesystem.exists(context, path)) {
            deleteFile(path);
        }
        WriteResult written = filesystem.write(context, path, content);
        if (!written.isSuccess()) {
            throw new SystemException("写入沙箱文件 [" + path + "] 失败: " + written.error());
        }
    }

    private void deleteFile(String path) {
        WriteResult deleted = filesystem.delete(RuntimeContext.empty(), path);
        if (!deleted.isSuccess()) {
            throw new SystemException("删除沙箱文件 [" + path + "] 失败: " + deleted.error());
        }
    }

    private String readText(String path) {
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

    private List<String> listNames(String directory) {
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
}
