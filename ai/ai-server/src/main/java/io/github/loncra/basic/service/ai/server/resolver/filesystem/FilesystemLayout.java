package io.github.loncra.basic.service.ai.server.resolver.filesystem;

import cn.hutool.core.text.AntPathMatcher;
import io.agentscope.harness.agent.workspace.WorkspaceConstants;
import io.github.loncra.framework.commons.CastUtils;
import org.apache.commons.io.IOUtils;

import java.nio.file.Path;
import java.util.List;

public final class FilesystemLayout {

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

    private FilesystemLayout() {
    }

    public static String segment(String principal) {
        return principal.replaceAll(PRINCIPAL_UNSAFE_PATTERN, CastUtils.UNDERSCORE);
    }

    public record UserLevelRoute(String prefix, String dir) {
    }

    public static final List<UserLevelRoute> USER_LEVEL_ROUTES = List.of(
            new UserLevelRoute(WorkspaceConstants.MEMORY_MD, WorkspaceConstants.MEMORY_DIR),
            new UserLevelRoute(WorkspaceConstants.MEMORY_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, WorkspaceConstants.MEMORY_DIR),
            new UserLevelRoute(WorkspaceConstants.AGENTS_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, WorkspaceConstants.AGENTS_DIR),
            new UserLevelRoute(WorkspaceConstants.SKILLS_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, WorkspaceConstants.SKILLS_DIR),
            new UserLevelRoute(HARNESS_BUS_PREFIX + AntPathMatcher.DEFAULT_PATH_SEPARATOR, BUS_DIR),
            new UserLevelRoute(TRANSCRIPTS_DIR + AntPathMatcher.DEFAULT_PATH_SEPARATOR, TRANSCRIPTS_DIR)
    );

    public static String storePrincipal(String home, String principal) {
        return join(home, segment(principal));
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

    public static String stateSession(String principal, String sessionId) {
        return join(segment(principal), STATE_DIR, segment(sessionId));
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
        return path.toString();
    }
}
