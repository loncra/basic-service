package io.github.loncra.basic.service.ai.server.constants;

/**
 * Clarify Mode 常量（对齐 AgentScope Plan Mode 工具命名风格）
 *
 * @author maurice.chen
 */
public final class ClarifyConstants {

    private ClarifyConstants() {
    }

    public static final String CLARIFY_ENTER = "clarify_enter";

    public static final String CLARIFY_WRITE = "clarify_write";

    public static final String CLARIFY_EXIT = "clarify_exit";

    /** AiMcpPackageEntity.metadata 根键 */
    public static final String METADATA_CLARIFY_KEY = "clarify";

    public static final String POLICY_ENABLED = "enabled";

    public static final String POLICY_TOOLS = "tools";

    public static final String POLICY_REQUIRED_DIMENSIONS = "requiredDimensions";

    public static final String POLICY_MAX_CALLS_PER_TURN = "maxCallsPerTurn";

    public static final String POLICY_MAX_CLARIFY_ROUNDS = "maxClarifyRounds";

    public static final String POLICY_FORM_TEMPLATE = "formTemplate";

    public static final String TEMPLATE_TITLE = "title";

    public static final String TEMPLATE_FIELDS = "fields";

    public static final String FIELD_KEY = "key";

    public static final String FIELD_WIDGET = "widget";

    public static final String FIELD_LABEL = "label";

    public static final String FIELD_OPTIONS = "options";

    public static final String FIELD_REQUIRED = "required";

    public static final String WIDGET_INPUT = "input";

    public static final String WIDGET_SELECT = "select";

    public static final String WIDGET_CHECKBOX = "checkbox";

    public static final String WIDGET_TEXTAREA = "textarea";

    /** 助手消息 metadata：澄清会话快照 */
    public static final String MESSAGE_CLARIFY_SESSION_KEY = "clarifySession";

    /** 回喂模型用的澄清答案（与 ConfirmResult 并列） */
    public static final String METADATA_CLARIFY_RESULTS = "clarifyResults";

    public static final String SESSION_ACTIVE = "active";

    public static final String SESSION_TARGET_TOOL = "targetTool";

    public static final String SESSION_CARD = "card";

    public static final String SESSION_WRITE_DONE = "writeDone";

    public static final String SESSION_ROUNDS = "rounds";

    public static final String SESSION_CALL_COUNTS = "callCounts";

    public static final String SESSION_DIMENSIONS_SATISFIED = "dimensionsSatisfied";

    public static final String SESSION_ANSWERS = "answers";

    public static final String TOOL_PARAM_TARGET_TOOL = "targetTool";

    public static final String TOOL_PARAM_CONTENT = "content";

    public static final String TOOL_PARAM_SUMMARY = "summary";

    public static final String CARD_COMMANDS = "commands";

    public static final String CARD_FIELDS = "fields";

    public static final String CARD_TITLE = "title";

    public static final String DENY_NEED_CLARIFY =
            "[Tool denied — clarify mode] Controlled MCP tool requires clarification BEFORE calling it. "
                    + "Do NOT retry this MCP tool. Call clarify_enter (if not active), then clarify_write "
                    + "with content.commands as A2UI v0.9 commands (TextField/SelectField/CheckboxField/"
                    + "ActionButton only; no textarea), covering all required dimensions in dataModel paths, "
                    + "then clarify_exit with a short summary for user approval.";

    public static final String DENY_CLARIFY_WHITELIST =
            "[Tool denied — clarify mode is active] Only clarify_enter / clarify_write / clarify_exit "
                    + "are allowed until the user submits the clarify form. Do NOT call controlled MCP tools.";

    public static final String DENY_BUDGET_EXCEEDED =
            "[Tool denied — call budget] Controlled tool call budget exceeded for this turn. "
                    + "Narrow the query via another clarify round or ask the user to restate the need.";

    public static final String ERR_EXIT_NEED_WRITE = "clarify_exit 前必须先成功 clarify_write（content.commands）";
}
