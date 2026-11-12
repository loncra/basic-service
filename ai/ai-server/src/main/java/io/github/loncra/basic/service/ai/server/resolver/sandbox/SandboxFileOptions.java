package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import cn.hutool.core.text.AntPathMatcher;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.model.LsResult;
import io.agentscope.harness.agent.filesystem.model.ReadResult;
import io.agentscope.harness.agent.filesystem.model.WriteResult;
import io.agentscope.harness.agent.filesystem.sandbox.AbstractSandboxFilesystem;
import io.github.loncra.framework.commons.exception.SystemException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.TokenParser;

import java.util.List;

public final class SandboxFileOptions {

    private static final String EMPTY_FILE = "System reminder: File exists but has empty contents";

    private SandboxFileOptions() {
    }

    public static void writeText(AbstractSandboxFilesystem filesystem, String path, String content) {
        RuntimeContext context = RuntimeContext.empty();
        if (filesystem.exists(context, path)) {
            deleteFile(filesystem, path);
        }
        WriteResult written = filesystem.write(context, path, content);
        if (!written.isSuccess()) {
            throw new SystemException("写入沙箱文件 [" + path + "] 失败: " + written.error());
        }
    }

    public static void deleteFile(AbstractSandboxFilesystem filesystem, String path) {
        WriteResult deleted = filesystem.delete(RuntimeContext.empty(), path);
        if (!deleted.isSuccess()) {
            throw new SystemException("删除沙箱文件 [" + path + "] 失败: " + deleted.error());
        }
    }

    public static String readText(AbstractSandboxFilesystem filesystem, String path) {
        ReadResult result = filesystem.read(RuntimeContext.empty(), path, 0, 0);
        if (!result.isSuccess() || result.fileData() == null || result.fileData().content() == null) {
            return null;
        }
        String content = result.fileData().content();
        if (content.isBlank() || content.startsWith(EMPTY_FILE)) {
            return StringUtils.EMPTY;
        }
        return content;
    }

    public static List<String> listNames(AbstractSandboxFilesystem filesystem, String directory) {
        LsResult result = filesystem.ls(RuntimeContext.empty(), directory);
        if (!result.isSuccess() || result.entries() == null) {
            return List.of();
        }
        return result.entries().stream()
                .map(entry -> {
                    String path = entry.path().replace(IOUtils.DIR_SEPARATOR_WINDOWS, AntPathMatcher.DEFAULT_PATH_SEPARATOR.charAt(0));
                    if (path.endsWith(AntPathMatcher.DEFAULT_PATH_SEPARATOR)) {
                        path = path.substring(0, path.length() - 1);
                    }
                    int slash = path.lastIndexOf(IOUtils.DIR_SEPARATOR_UNIX);
                    return slash < 0 ? path : path.substring(slash + 1);
                })
                .filter(name -> !name.isEmpty())
                .toList();
    }
}
