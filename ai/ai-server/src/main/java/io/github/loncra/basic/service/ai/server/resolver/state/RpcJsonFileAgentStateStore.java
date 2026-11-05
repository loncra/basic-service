package io.github.loncra.basic.service.ai.server.resolver.state;

import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import io.agentscope.harness.agent.filesystem.model.FileData;
import io.github.loncra.basic.service.ai.server.enumerate.RpcFilesystemOperationEnum;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.RpcFilesystemInvoker;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemProperties;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RpcJsonFileAgentStateStore extends JsonFileAgentStateStore {

    public static final String AGENT_STATE_KEY = "agent_state";

    public static final String MEMORY_MESSAGES_KEY = "memory_messages";

    private static final String LOCAL_STATE_DIR_NAME = "loncra-rpc-state-store";

    private static final String ROOT_PATH = "/workspace/.agentscope-state";

    private static final String PATH_FIELD_NAME = "path";

    private static final String OFFSET_FIELD_NAME = "offset";

    private static final String LIMIT_FIELD_NAME = "limit";

    private static final String CONTENT_FIELD_NAME = "content";

    private final RpcFilesystemInvoker invoker;

    private final String deviceIdentified;

    public RpcJsonFileAgentStateStore(RpcFilesystemInvoker invoker, String deviceIdentified) {
        super(Path.of(SystemProperties.getJavaIoTmpdir(), LOCAL_STATE_DIR_NAME));
        this.invoker = invoker;
        this.deviceIdentified = deviceIdentified;
    }

    @Override
    public void save(String userId, String sessionId, String key, State value) {
        String json = JsonUtils.getJsonCodec().toPrettyJson(value);
        writeString(stateFilePath(userId, sessionId, key), json);
    }

    @Override
    public void save(String userId, String sessionId, String key, List<? extends State> values) {
        String content = values.stream()
                .map(s -> JsonUtils.getJsonCodec().toJson(s))
                .reduce((a, b) -> a + StringUtils.LF + b)
                .orElse(StringUtils.EMPTY);
        writeString(listFilePath(userId, sessionId, key), content);
    }

    @Override
    public <T extends State> Optional<T> get(String userId, String sessionId, String key, Class<T> type) {
        String json = readString(stateFilePath(userId, sessionId, key));
        if (Objects.isNull(json)) {
            return Optional.empty();
        }
        return Optional.of(SystemException.convertSupplier(() -> JsonUtils.getJsonCodec().fromJson(json, type)));
    }

    @Override
    public <T extends State> List<T> getList(String userId, String sessionId, String key, Class<T> itemType) {
        String content = readString(listFilePath(userId, sessionId, key));
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
        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.EXISTS, Map.of(PATH_FIELD_NAME, sessionDir(userId, sessionId)));
        return result.isSuccess() && Boolean.TRUE.equals(result.getData());
    }

    @Override
    public void delete(String userId, String sessionId, String key) {
        deleteFile(stateFilePath(userId, sessionId, key));
        deleteFile(listFilePath(userId, sessionId, key));
        deleteFile(hashFilePath(userId, sessionId, key));
    }

    @Override
    public void delete(String userId, String sessionId) {
        delete(userId, sessionId, AGENT_STATE_KEY);
        delete(userId, sessionId, MEMORY_MESSAGES_KEY);
    }

    private void writeString(String path, String content) {
        RestResult<Object> result = invoke(
                RpcFilesystemOperationEnum.WRITE,
                Map.of(PATH_FIELD_NAME, path, CONTENT_FIELD_NAME, content)
        );
        SystemException.isTrue(result.isSuccess(), "写入客户端状态文件 [" + path + "] 失败: " + result.getMessage());
    }

    private String readString(String path) {
        RestResult<Object> result = invoke(
                RpcFilesystemOperationEnum.READ,
                Map.of(PATH_FIELD_NAME, path, OFFSET_FIELD_NAME, 0, LIMIT_FIELD_NAME, 0)
        );
        if (!result.isSuccess()) {
            return null;
        }
        FileData fileData = CastUtils.convertValue(result.getData(), FileData.class);
        return Objects.isNull(fileData) ? null : fileData.content();
    }

    private void deleteFile(String path) {
        invoke(RpcFilesystemOperationEnum.DELETE, Map.of(PATH_FIELD_NAME, path));
    }

    private RestResult<Object> invoke(RpcFilesystemOperationEnum operation, Map<String, Object> params) {
        return invoker.invoke(deviceIdentified, operation, params);
    }

    private String stateFilePath(String userId, String sessionId, String key) {
        return sessionDir(userId, sessionId) + "/" + key + ".json";
    }

    private String listFilePath(String userId, String sessionId, String key) {
        return sessionDir(userId, sessionId) + "/" + key + ".jsonl";
    }

    private String hashFilePath(String userId, String sessionId, String key) {
        return sessionDir(userId, sessionId) + "/" + key + ".hash";
    }

    private String sessionDir(String userId, String sessionId) {
        return ROOT_PATH + "/" + safeSegment(normalizeUser(userId)) + "/" + safeSegment(sessionId);
    }

    private String normalizeUser(String userId) {
        return Objects.isNull(userId) || userId.isBlank() ? "__anon__" : userId;
    }

    private String safeSegment(String value) {
        return value.replaceAll("[^a-zA-Z0-9_\\-.]", "_");
    }
}
