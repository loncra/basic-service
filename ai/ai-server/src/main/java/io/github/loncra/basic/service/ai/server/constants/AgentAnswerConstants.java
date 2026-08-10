package io.github.loncra.basic.service.ai.server.constants;

/**
 * 助手 answer 块扩展字段（HITL exit 合成应答）
 */
public final class AgentAnswerConstants {

    private AgentAnswerConstants() {
    }

    public static final String FORMAT = "format";

    public static final String FORMAT_MARKDOWN = "markdown";

    public static final String FORMAT_A2UI = "a2ui";

    public static final String SOURCE_EXIT = "sourceExit";

    public static final String HITL_TOOL_CALL_ID = "hitlToolCallId";

    public static final String COMMANDS = "commands";

    public static final String SURFACE_ID = "surfaceId";

    /** AgentScope Plan Mode exit 工具名 */
    public static final String PLAN_EXIT = "plan_exit";

    public static final String PLAN_WRITE = "plan_write";

    public static final String ACTION_CLARIFY_SUBMIT = "clarify.submit";

    public static final String ACTION_CLARIFY_CANCEL = "clarify.cancel";
}
