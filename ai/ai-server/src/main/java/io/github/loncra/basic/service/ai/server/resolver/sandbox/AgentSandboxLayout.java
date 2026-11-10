package io.github.loncra.basic.service.ai.server.resolver.sandbox;

import io.github.loncra.framework.commons.CastUtils;

import java.nio.file.Path;

public final class AgentSandboxLayout {

    public static final String DATA_DIR = ".loncra-agent";

    public static final String CONVERSATIONS_DIR = "conversations";

    public static final String MESSAGES_DIR = "messages";

    public static final String INDEXES_DIR = "indexes";

    public static final String STATE_DIR = "state";

    public static final String JSON_SUFFIX = ".json";

    public static final String JSONL_SUFFIX = ".jsonl";

    public static final String PRINCIPAL_UNSAFE_PATTERN = "[^a-zA-Z0-9_\\-.]";

    private AgentSandboxLayout() {
    }

    public static String segment(String principal) {
        return principal.replaceAll(PRINCIPAL_UNSAFE_PATTERN, CastUtils.UNDERSCORE);
    }

    public static String storePrincipal(String home, String principal) {
        return join(home, DATA_DIR, segment(principal));
    }

    public static String conversationFile(String home, String principal, String conversationId) {
        return join(storePrincipal(home, principal), CONVERSATIONS_DIR, conversationId + JSON_SUFFIX);
    }

    public static String conversationsDir(String home, String principal) {
        return join(storePrincipal(home, principal), CONVERSATIONS_DIR);
    }

    public static String messageFile(String home, String principal, String messageId) {
        return join(storePrincipal(home, principal), MESSAGES_DIR, messageId + JSON_SUFFIX);
    }

    public static String messageIndexFile(String home, String principal, String conversationId) {
        return join(storePrincipal(home, principal), INDEXES_DIR, conversationId + JSON_SUFFIX);
    }

    public static String stateSession(String home, String principal, String sessionId) {
        return join(storePrincipal(home, principal), STATE_DIR, segment(sessionId));
    }

    public static String stateDir(String home, String principal) {
        return join(storePrincipal(home, principal), STATE_DIR);
    }

    public static String stateFile(String home, String principal, String sessionId, String key) {
        return join(stateSession(home, principal, sessionId), key + JSON_SUFFIX);
    }

    public static String stateListFile(String home, String principal, String sessionId, String key) {
        return join(stateSession(home, principal, sessionId), key + JSONL_SUFFIX);
    }

    public static Path workspaceDirectory(Path workspacePath, String workspaceId) {
        return workspacePath.resolve(workspaceId);
    }

    private static String join(String root, String... parts) {
        StringBuilder path = new StringBuilder(root.replace('\\', '/'));
        while (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
            path.setLength(path.length() - 1);
        }
        for (String part : parts) {
            path.append('/').append(part);
        }
        return path.toString();
    }
}
