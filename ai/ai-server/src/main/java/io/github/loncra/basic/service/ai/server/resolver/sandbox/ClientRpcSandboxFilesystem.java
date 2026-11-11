package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import com.fasterxml.jackson.core.type.TypeReference;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.model.*;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.github.loncra.basic.service.ai.server.enumerate.agent.RpcFilesystemOperationEnum;
import io.github.loncra.basic.service.message.api.service.SocketServiceClient;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.socketio.api.metadata.UnicastMessageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.util.*;

@Slf4j
public class ClientRpcSandboxFilesystem implements AbstractSandboxFilesystem {

    private static final String REQUEST_EVENT_NAME = "agent_filesystem_request";

    private static final int REQUEST_TIMEOUT_SECONDS = 60;

    private static final List<String> PATH_PARAM_FIELDS = List.of("path", "fromPath", "toPath");

    private final SocketServiceClient socketServiceClient;

    private final String deviceIdentified;

    private final RpcFilesystemScope scope;

    public ClientRpcSandboxFilesystem(
            SocketServiceClient socketServiceClient,
            String deviceIdentified,
            RpcFilesystemScope scope
    ) {
        this.socketServiceClient = socketServiceClient;
        this.deviceIdentified = deviceIdentified;
        this.scope = scope;
    }

    private String mapPath(String path) {
        if (!(scope instanceof RpcFilesystemScope.Store)) {
            return path;
        }
        return StringUtils.stripStart(path, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
    }

    private Map<String, Object> mapParams(Map<String, Object> params) {
        Map<String, Object> mapped = new LinkedHashMap<>(params);
        for (String field : PATH_PARAM_FIELDS) {
            Object value = mapped.get(field);
            if (value instanceof String path) {
                mapped.put(field, mapPath(path));
            }
        }
        return mapped;
    }

    @Override
    public String id() {
        return "client-" + deviceIdentified;
    }

    @Override
    public LsResult ls(
            RuntimeContext runtimeContext,
            String path
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (path != null) {
            params.put("path", path);
        }
        RestResult<Object> result = call(RpcFilesystemOperationEnum.LS, params);
        if (!result.isSuccess()) {
            return LsResult.fail(result.getMessage());
        }
        return LsResult.success(files(map(result.getData()).get("entries")));
    }

    @Override
    public ReadResult read(
            RuntimeContext runtimeContext,
            String filePath,
            int offset,
            int limit
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("path", filePath);
        params.put("offset", offset);
        params.put("limit", limit);
        RestResult<Object> result = call(RpcFilesystemOperationEnum.READ, params);
        if (!result.isSuccess()) {
            return ReadResult.fail(result.getMessage());
        }
        return ReadResult.success(FileData.create(Objects.toString(map(result.getData()).get("content"), "")));
    }

    @Override
    public WriteResult write(
            RuntimeContext runtimeContext,
            String filePath,
            String content
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("path", filePath);
        params.put("content", content);
        params.put("overwrite", false);
        RestResult<Object> result = call(RpcFilesystemOperationEnum.WRITE, params);
        if (!result.isSuccess()) {
            return WriteResult.fail(result.getMessage());
        }
        return WriteResult.ok(Objects.toString(map(result.getData()).get("path"), filePath));
    }

    @Override
    public EditResult edit(
            RuntimeContext runtimeContext,
            String filePath,
            String oldString,
            String newString,
            boolean replaceAll
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("path", filePath);
        params.put("oldString", oldString);
        params.put("newString", newString);
        params.put("replaceAll", replaceAll);
        RestResult<Object> result = call(RpcFilesystemOperationEnum.EDIT, params);
        if (!result.isSuccess()) {
            return EditResult.fail(result.getMessage());
        }
        Map<String, Object> body = map(result.getData());
        return EditResult.ok(Objects.toString(body.get("path"), filePath), number(body.get("occurrences")));
    }

    @Override
    public GrepResult grep(
            RuntimeContext runtimeContext,
            String pattern,
            String path,
            String glob
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pattern", pattern);
        params.put("path", path);
        params.put("glob", glob);
        RestResult<Object> result = call(RpcFilesystemOperationEnum.GREP, params);
        if (!result.isSuccess()) {
            return GrepResult.fail(result.getMessage());
        }
        List<GrepMatch> matches = rows(map(result.getData()).get("matches")).stream()
                .map(row -> new GrepMatch(Objects.toString(row.get("path"), ""), number(row.get("line")), Objects.toString(row.get("text"), "")))
                .toList();
        return GrepResult.success(matches);
    }

    @Override
    public GlobResult glob(
            RuntimeContext runtimeContext,
            String pattern,
            String path
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pattern", pattern);
        params.put("path", path);
        RestResult<Object> result = call(RpcFilesystemOperationEnum.GLOB, params);
        if (!result.isSuccess()) {
            return GlobResult.fail(result.getMessage());
        }
        return GlobResult.success(files(map(result.getData()).get("matches")));
    }

    @Override
    public List<FileUploadResponse> uploadFiles(
            RuntimeContext runtimeContext,
            List<Map.Entry<String, byte[]>> files
    ) {
        List<Map<String, String>> payload = files.stream()
                .map(entry -> Map.of("path", mapPath(entry.getKey()), "contentBase64", Base64.getEncoder().encodeToString(entry.getValue())))
                .toList();
        RestResult<Object> result = call(RpcFilesystemOperationEnum.UPLOAD, Map.of("files", payload));
        if (!result.isSuccess()) {
            return List.of(FileUploadResponse.fail("", result.getMessage()));
        }
        return rows(result.getData()).stream()
                .map(row -> {
                    String itemPath = Objects.toString(row.get("path"), "");
                    Object error = row.get("error");
                    return error == null ? FileUploadResponse.success(itemPath) : FileUploadResponse.fail(itemPath, error.toString());
                })
                .toList();
    }

    @Override
    public List<FileDownloadResponse> downloadFiles(
            RuntimeContext runtimeContext,
            List<String> paths
    ) {
        RestResult<Object> result = call(RpcFilesystemOperationEnum.DOWNLOAD, Map.of("paths", paths.stream().map(this::mapPath).toList()));
        if (!result.isSuccess()) {
            return List.of(FileDownloadResponse.fail("", result.getMessage()));
        }
        return rows(result.getData()).stream()
                .map(row -> {
                    String itemPath = Objects.toString(row.get("path"), "");
                    Object error = row.get("error");
                    if (error != null) {
                        return FileDownloadResponse.fail(itemPath, error.toString());
                    }
                    return FileDownloadResponse.success(itemPath, Base64.getDecoder().decode(Objects.toString(row.get("contentBase64"), "")));
                })
                .toList();
    }

    @Override
    public WriteResult delete(
            RuntimeContext runtimeContext,
            String path
    ) {
        RestResult<Object> result = call(RpcFilesystemOperationEnum.DELETE, Map.of("path", path));
        if (!result.isSuccess()) {
            return WriteResult.fail(result.getMessage());
        }
        return WriteResult.ok(Objects.toString(map(result.getData()).get("path"), path));
    }

    @Override
    public WriteResult move(
            RuntimeContext runtimeContext,
            String fromPath,
            String toPath
    ) {
        RestResult<Object> result = call(RpcFilesystemOperationEnum.MOVE, Map.of("fromPath", fromPath, "toPath", toPath));
        if (!result.isSuccess()) {
            return WriteResult.fail(result.getMessage());
        }
        return WriteResult.ok(Objects.toString(map(result.getData()).get("path"), toPath));
    }

    @Override
    public boolean exists(
            RuntimeContext runtimeContext,
            String path
    ) {
        RestResult<Object> result = call(RpcFilesystemOperationEnum.EXISTS, Map.of("path", path));
        return result.isSuccess() && Boolean.TRUE.equals(map(result.getData()).get("exists"));
    }

    @Override
    public ExecuteResponse execute(
            RuntimeContext runtimeContext,
            String command,
            Integer timeoutSeconds
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("command", command);
        params.put("timeoutSeconds", timeoutSeconds == null ? 60 : timeoutSeconds);
        RestResult<Object> result = call(RpcFilesystemOperationEnum.EXECUTE, params);
        if (!result.isSuccess()) {
            return new ExecuteResponse(result.getMessage(), 1, false);
        }
        Map<String, Object> body = map(result.getData());
        String stdout = Objects.toString(body.get("stdout"), "");
        String stderr = Objects.toString(body.get("stderr"), "");
        String output = stderr.isBlank() ? stdout : stdout.isBlank() ? stderr : stdout + "\n" + stderr;
        return new ExecuteResponse(output, number(result.getExecuteCode()), Boolean.TRUE.equals(body.get("truncated")));
    }

    private RestResult<Object> call(
            RpcFilesystemOperationEnum operation,
            Map<String, Object> params
    ) {
        if (StringUtils.isEmpty(deviceIdentified)) {
            return RestResult.of("当前会话没有绑定客户端设备，无法执行文件系统操作", 500, "500");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("operation", operation.getValue());
        payload.put("params", mapParams(params));
        payload.put("scope", scope.toPayload());
        UnicastMessageMetadata<Map<String, Object>> metadata = UnicastMessageMetadata.of(
                deviceIdentified,
                REQUEST_EVENT_NAME,
                RestResult.ofSuccess(payload)
        );
        metadata.getMetadata().put(TimeProperties.class.getName(), TimeProperties.ofSeconds(REQUEST_TIMEOUT_SECONDS));
        try {
            List<RestResult<?>> results = socketServiceClient.ackUnicast(List.of(metadata));
            if (CollectionUtils.isEmpty(results) || Objects.isNull(results.getFirst().getData())) {
                return RestResult.of("客户端 [" + deviceIdentified + "] 无应答", 500, "500");
            }
            Map<String, Object> ack = CastUtils.convertValue(results.getFirst().getData(), CastUtils.MAP_TYPE_REFERENCE);
            Object message = ack.get("message");
            Object value = message instanceof Map<?, ?> map ? map.get("value") : null;
            if (Objects.isNull(value)) {
                return RestResult.of("客户端 [" + deviceIdentified + "] 响应超时", 500, "500");
            }
            RestResult<Object> response = CastUtils.convertValue(
                    value, new TypeReference<>() {
                    }
            );
            if (Objects.isNull(response)) {
                return RestResult.of("客户端 [" + deviceIdentified + "] 返回内容为空", 500, "500");
            }
            return response;
        } catch (Exception exception) {
            log.warn("客户端文件系统请求失败, deviceIdentified={}, operation={}", deviceIdentified, operation.getValue(), exception);
            return RestResult.of("客户端文件系统请求失败: " + exception.getMessage(), 500, "500");
        }
    }

    private static Map<String, Object> map(Object value) {
        if (value == null) {
            return Map.of();
        }
        return CastUtils.convertValue(value, CastUtils.MAP_TYPE_REFERENCE);
    }

    private static List<Map<String, Object>> rows(Object value) {
        if (value == null) {
            return List.of();
        }
        return CastUtils.convertValue(value, CastUtils.LIST_MAP_TYPE_REFERENCE);
    }

    private static List<FileInfo> files(Object value) {
        return rows(value).stream()
                .map(row -> new FileInfo(
                        Objects.toString(row.get("path"), ""),
                        Boolean.TRUE.equals(row.get("isDirectory")),
                        number(row.get("size")),
                        Objects.toString(row.get("modifiedAt"), "")
                ))
                .toList();
    }

    private static int number(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text);
        }
        return 0;
    }
}
