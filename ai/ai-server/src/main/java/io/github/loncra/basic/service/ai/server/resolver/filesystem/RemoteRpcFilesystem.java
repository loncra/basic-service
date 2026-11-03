package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.model.EditResult;
import io.agentscope.harness.agent.filesystem.model.ExecuteResponse;
import io.agentscope.harness.agent.filesystem.model.FileData;
import io.agentscope.harness.agent.filesystem.model.FileDownloadResponse;
import io.agentscope.harness.agent.filesystem.model.FileInfo;
import io.agentscope.harness.agent.filesystem.model.FileUploadResponse;
import io.agentscope.harness.agent.filesystem.model.GlobResult;
import io.agentscope.harness.agent.filesystem.model.GrepMatch;
import io.agentscope.harness.agent.filesystem.model.GrepResult;
import io.agentscope.harness.agent.filesystem.model.LsResult;
import io.agentscope.harness.agent.filesystem.model.ReadResult;
import io.agentscope.harness.agent.filesystem.model.WriteResult;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.github.loncra.basic.service.ai.server.enumerate.RpcFilesystemOperationEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class RemoteRpcFilesystem implements AbstractSandboxFilesystem {

    private static final String FILESYSTEM_ID = "client-rpc";

    private static final String PATH_FIELD_NAME = "path";

    private static final String OFFSET_FIELD_NAME = "offset";

    private static final String LIMIT_FIELD_NAME = "limit";

    private static final String PATTERN_FIELD_NAME = "pattern";

    private static final String GLOB_FIELD_NAME = "glob";

    private static final String CONTENT_FIELD_NAME = "content";

    private static final String OLD_STRING_FIELD_NAME = "oldString";

    private static final String NEW_STRING_FIELD_NAME = "newString";

    private static final String REPLACE_ALL_FIELD_NAME = "replaceAll";

    private static final String FROM_PATH_FIELD_NAME = "fromPath";

    private static final String TO_PATH_FIELD_NAME = "toPath";

    private static final String COMMAND_FIELD_NAME = "command";

    private static final String TIMEOUT_SECONDS_FIELD_NAME = "timeoutSeconds";

    private final RpcFilesystemInvoker invoker;

    private final String deviceIdentified;

    @Override
    public String id() {
        return FILESYSTEM_ID;
    }

    @Override
    public LsResult ls(RuntimeContext runtimeContext, String path) {

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.LS, Map.of(PATH_FIELD_NAME, path));

        if (!result.isSuccess()) {
            return LsResult.fail(result.getMessage());
        }

        return LsResult.success(
                CastUtils.convertValue(result.getData(), new TypeReference<List<FileInfo>>() {})
        );
    }

    @Override
    public ReadResult read(RuntimeContext runtimeContext, String filePath, int offset, int limit) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PATH_FIELD_NAME, filePath);
        params.put(OFFSET_FIELD_NAME, offset);
        params.put(LIMIT_FIELD_NAME, limit);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.READ, params);

        if (!result.isSuccess()) {
            return ReadResult.fail(result.getMessage());
        }

        return ReadResult.success(CastUtils.convertValue(result.getData(), FileData.class));
    }

    @Override
    public WriteResult write(RuntimeContext runtimeContext, String filePath, String content) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PATH_FIELD_NAME, filePath);
        params.put(CONTENT_FIELD_NAME, content);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.WRITE, params);

        if (!result.isSuccess()) {
            return WriteResult.fail(result.getMessage());
        }

        return WriteResult.ok(filePath);
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
        params.put(PATH_FIELD_NAME, filePath);
        params.put(OLD_STRING_FIELD_NAME, oldString);
        params.put(NEW_STRING_FIELD_NAME, newString);
        params.put(REPLACE_ALL_FIELD_NAME, replaceAll);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.EDIT, params);

        if (!result.isSuccess()) {
            return EditResult.fail(result.getMessage());
        }

        return CastUtils.convertValue(result.getData(), EditResult.class);
    }

    @Override
    public GrepResult grep(RuntimeContext runtimeContext, String pattern, String path, String glob) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PATTERN_FIELD_NAME, pattern);
        params.put(PATH_FIELD_NAME, path);
        params.put(GLOB_FIELD_NAME, glob);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.GREP, params);

        if (!result.isSuccess()) {
            return GrepResult.fail(result.getMessage());
        }

        return GrepResult.success(
                CastUtils.convertValue(result.getData(), new TypeReference<List<GrepMatch>>() {})
        );
    }

    @Override
    public GlobResult glob(RuntimeContext runtimeContext, String pattern, String path) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PATTERN_FIELD_NAME, pattern);
        params.put(PATH_FIELD_NAME, path);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.GLOB, params);

        if (!result.isSuccess()) {
            return GlobResult.fail(result.getMessage());
        }

        return GlobResult.success(
                CastUtils.convertValue(result.getData(), new TypeReference<List<FileInfo>>() {})
        );
    }

    @Override
    public boolean exists(RuntimeContext runtimeContext, String path) {

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.EXISTS, Map.of(PATH_FIELD_NAME, path));

        return result.isSuccess() && Boolean.TRUE.equals(result.getData());
    }

    @Override
    public WriteResult delete(RuntimeContext runtimeContext, String path) {

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.DELETE, Map.of(PATH_FIELD_NAME, path));

        if (!result.isSuccess()) {
            return WriteResult.fail(result.getMessage());
        }

        return WriteResult.ok(path);
    }

    @Override
    public WriteResult move(RuntimeContext runtimeContext, String fromPath, String toPath) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(FROM_PATH_FIELD_NAME, fromPath);
        params.put(TO_PATH_FIELD_NAME, toPath);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.MOVE, params);

        if (!result.isSuccess()) {
            return WriteResult.fail(result.getMessage());
        }

        return WriteResult.ok(toPath);
    }

    @Override
    public ExecuteResponse execute(
            RuntimeContext runtimeContext,
            String command,
            Integer timeoutSeconds
    ) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(COMMAND_FIELD_NAME, command);
        params.put(TIMEOUT_SECONDS_FIELD_NAME, timeoutSeconds);

        RestResult<Object> result = invoke(RpcFilesystemOperationEnum.EXECUTE, params);

        if (!result.isSuccess()) {
            return new ExecuteResponse(result.getMessage(), -1, false);
        }

        return CastUtils.convertValue(result.getData(), ExecuteResponse.class);
    }

    @Override
    public List<FileUploadResponse> uploadFiles(
            RuntimeContext runtimeContext,
            List<Map.Entry<String, byte[]>> files
    ) {
        return files
                .stream()
                .map(s -> FileUploadResponse.fail(s.getKey(), "客户端文件系统暂不支持 uploadFiles"))
                .toList();
    }

    @Override
    public List<FileDownloadResponse> downloadFiles(
            RuntimeContext runtimeContext,
            List<String> paths
    ) {
        return paths
                .stream()
                .map(s -> FileDownloadResponse.fail(s, "客户端文件系统暂不支持 downloadFiles"))
                .toList();
    }

    private RestResult<Object> invoke(
            RpcFilesystemOperationEnum operation,
            Map<String, Object> params
    ) {

        RestResult<Object> result = invoker.invoke(deviceIdentified, operation, params);

        if (Objects.isNull(result)) {
            return RestResult.of("客户端未返回文件系统结果", 500);
        }

        if (log.isInfoEnabled()) {
            log.info(
                    "客户端文件系统调用, deviceIdentified={}, operation={}, params={}, success={}, message={}",
                    deviceIdentified,
                    operation,
                    params,
                    result.isSuccess(),
                    result.getMessage()
            );
        }

        return result;
    }

}
