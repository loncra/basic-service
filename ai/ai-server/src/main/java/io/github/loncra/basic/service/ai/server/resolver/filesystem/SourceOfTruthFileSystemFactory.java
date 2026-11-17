package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import cn.hutool.core.text.AntPathMatcher;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileData;
import io.agentscope.harness.agent.workspace.WorkspaceConstants;
import io.github.loncra.basic.service.ai.server.config.SourceOfTruthConfig;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SourceOfTruthFileSystemFactory {

    public static final String BUS_DIR = "bus";

    public static final String TRANSCRIPTS_DIR = "transcripts";

    public static final String HARNESS_BUS_PREFIX = ".agentscope";

    public static final String CONVERSATIONS_DIR = "conversations";

    public static final String MESSAGES_DIR = "messages";

    public static final String INDEXES_DIR = "indexes";

    public static final String STATE_DIR = "state";

    public static final String JSON_SUFFIX = ".json";

    public static final String JSONL_SUFFIX = ".jsonl";

    public static final String PRINCIPAL_UNSAFE_PATTERN = "[^a-zA-Z0-9_\\-.]";

    public static final List<UserLevelRoute> USER_LEVEL_ROUTES = List.of(
            new UserLevelRoute(WorkspaceConstants.MEMORY_MD, WorkspaceConstants.MEMORY_DIR),
            new UserLevelRoute(WorkspaceConstants.MEMORY_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, WorkspaceConstants.MEMORY_DIR),
            new UserLevelRoute(WorkspaceConstants.AGENTS_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, WorkspaceConstants.AGENTS_DIR),
            new UserLevelRoute(WorkspaceConstants.SKILLS_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, WorkspaceConstants.SKILLS_DIR),
            new UserLevelRoute(HARNESS_BUS_PREFIX + AntPathMatcher.DEFAULT_PATH_SEPARATOR, BUS_DIR),
            new UserLevelRoute(TRANSCRIPTS_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, TRANSCRIPTS_DIR)
    );

    private final List<SourceOfTruthFileSystemConstructor> constructors;

    @Getter
    private final SourceOfTruthConfig sourceOfTruthConfig;

    public record UserLevelRoute(String prefix, String dir) {
    }

    public record PathFileData(String path, FileData fileData) {
        public String content()  { return fileData.content(); }
        public String encoding() { return fileData.encoding(); }
    }

    public AbstractFilesystem getUserDataDirFilesystem(
            String principal,
            String dir
    ) {
        String segment = segment(principal);
        String relativePath = String.join(org.springframework.util.AntPathMatcher.DEFAULT_PATH_SEPARATOR, sourceOfTruthConfig.getDataDir(), segment, dir);
        return getSourceOfTruthFileSystem(relativePath);
    }

    public AbstractFilesystem getSourceOfTruthFileSystem(String relativePath) {
        return constructors.stream()
                .filter(s -> s.getType().equals(sourceOfTruthConfig.getType()))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到类型为 [" + sourceOfTruthConfig.getType() + "] 的构造器"))
                .getSourceOfTruthFileSystem(sourceOfTruthConfig, relativePath);
    }

    public static String segment(String principal) {
        return principal.replaceAll(PRINCIPAL_UNSAFE_PATTERN, CastUtils.UNDERSCORE);
    }


    public static String storePrincipal(String home, String principal) {
        String path = join(home, segment(principal));
        return path.startsWith(AntPathMatcher.DEFAULT_PATH_SEPARATOR)
                ? path.substring(1)
                : path;
    }

    public static String stateSession(String principal, String sessionId) {
        return join(segment(principal), STATE_DIR, segment(sessionId));
    }

    public static String conversationFile(String principal, String conversationId) {
        return join(segment(principal), CONVERSATIONS_DIR, conversationId + JSON_SUFFIX);
    }

    public static String conversationsDir(String principal) {
        return join(segment(principal), CONVERSATIONS_DIR);
    }

    public static String messageFile(String principal, String messageId) {
        return join(segment(principal), MESSAGES_DIR, messageId + JSON_SUFFIX);
    }

    public static String messageIndexFile(String principal, String conversationId) {
        return join(segment(principal), INDEXES_DIR, conversationId + JSON_SUFFIX);
    }

    public static String stateDir(String principal) {
        return join(segment(principal), STATE_DIR);
    }

    public static String stateFile(String principal, String sessionId, String key) {
        return join(stateSession(principal, sessionId), key + JSON_SUFFIX);
    }

    public static String transcriptsDir(String principal) {
        return join(segment(principal), TRANSCRIPTS_DIR);
    }

    public static String stateListFile(String principal, String sessionId, String key) {
        return join(stateSession(principal, sessionId), key + JSONL_SUFFIX);
    }

    public static Path workspaceDirectory(Path workspacePath, String workspaceId) {
        return workspacePath.resolve(workspaceId);
    }

    private static String join(String root, String... parts) {
        StringBuilder path = new StringBuilder(root.replace(IOUtils.DIR_SEPARATOR_WINDOWS, AntPathMatcher.DEFAULT_PATH_SEPARATOR.charAt(0)));
        while (path.length() > 1 && path.charAt(path.length() - 1) == AntPathMatcher.DEFAULT_PATH_SEPARATOR.charAt(0)) {
            path.setLength(path.length() - 1);
        }
        for (String part : parts) {
            path.append(AntPathMatcher.DEFAULT_PATH_SEPARATOR).append(part);
        }
        if (path.charAt(0) != AntPathMatcher.DEFAULT_PATH_SEPARATOR.charAt(0)) {
            path.insert(0, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
        }
        return path.toString();
    }
}
