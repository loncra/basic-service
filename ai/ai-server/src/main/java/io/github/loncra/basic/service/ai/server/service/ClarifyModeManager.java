package io.github.loncra.basic.service.ai.server.service;

import io.agentscope.core.state.AgentState;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;
import io.github.loncra.basic.service.ai.api.enumerate.ClarifyModeStatusEnum;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.ClarifyModeContextState;
import io.github.loncra.basic.service.ai.server.domain.metadata.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.basic.service.ai.server.resolver.AgentToolkitContributor;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClarifyModeManager implements AgentToolkitContributor {

    public static final String STATE_KEY = "clarify_mode";

    public static final String CLARIFY_MCP_NAME_KEY = "clarify_mcp_name";

    public static final String CLARIFY_TOOL_NAME_KEY = "clarify_tool_name";

    public static final String CLARIFY_ENTER = "clarify_enter";

    //public static final String CLARIFY_ASK = "clarify_ask";

    public static final String CLARIFY_EXIT = "clarify_exit";

    private final AgentStateStore agentStateStore;

    private final AiAppConfig aiAppConfig;

    private final AiMcpPackageService aiMcpPackageService;

    public ClarifyModeContextState getOrCreateClarifyModeContextState(
            String userId,
            String sessionId
    ) {
        return agentStateStore.get(userId, sessionId, STATE_KEY, ClarifyModeContextState.class)
                .orElseGet(() -> createClarifyModeContextState(userId, sessionId));
    }

    public ClarifyModeContextState enter(
            AgentState state,
            int maxClarifyRounds
    ) {

        ClarifyModeContextState ctx = getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
        ctx.setStatus(ClarifyModeStatusEnum.PENDING);
        ctx.setMaxClarifyRounds(maxClarifyRounds);
        ctx.setQuestions(new LinkedList<>());

        agentStateStore.save(state.getUserId(), state.getSessionId(), STATE_KEY, ctx);
        return ctx;
    }

    @Tool(
            name = CLARIFY_ENTER,
            stateInjected = true,
            description = "Enter CLARIFY mode before any gated MCP tool."
                    + "Gated tools are blocked until " + CLARIFY_EXIT + "."
                    + "After success: if info is missing, ask exactly ONE plain-chat question with  A / B / C (and more if needed) options, then wait; if already clear, call " + CLARIFY_EXIT + " immediately and then the gated tool."
    )
    public String enterTool(
            @ToolParam(
                    name = CLARIFY_MCP_NAME_KEY,
                    description = "MCP name (MCP group id)"
            )
            String mcpName,
            @ToolParam(
                    name = CLARIFY_TOOL_NAME_KEY,
                    description = "MCP tool name (target gated tool)"
            )
            String mcpTool,
            AgentState state
    ) {
        SystemException.isTrue(Objects.nonNull(state), "Error: agent state unavailable.");
        if (StringUtils.isAnyBlank(mcpName, mcpTool)) {
            return "Error: " + CLARIFY_MCP_NAME_KEY + " and " + CLARIFY_TOOL_NAME_KEY + " are required.";
        }

        Optional<McpClarifyToolPolicyMetadata> optional =
                aiMcpPackageService.getMcpClientCacheClarifyToolPolicyMetadata(mcpName, mcpTool);
        if (optional.isEmpty()) {
            return "Error: tool not found.";
        }

        McpClarifyToolPolicyMetadata metadata = optional.get();
        if (!metadata.getEnabled().toBoolean()) {
            return "Error: tool is not gated.";
        }

        ClarifyModeContextState contextState =
                enter(state, Objects.requireNonNullElse(metadata.getMaxClarifyRounds(), aiAppConfig.getDefaultMaxClarifyRounds()));
        return "Entered CLARIFY mode (" + contextState.getStatus().toString() + "). Gated MCP tools are still blocked."
                + "Next: ask exactly ONE clarifying question in plain chat with A / B / C (and more if needed) options, then stop and wait for the user."
                + " If you already have enough information, call clarify_exit now (no question), then call the gated tool."
                + " Do not ask multiple questions in one message.";
    }

    /**
     * Exits plan mode (back to BUILD). Idempotent. Keeps {@code currentPlanFile} for reference.
     */
    public void exit(AgentState state) {
        ClarifyModeContextState ctx = getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
        ctx.setStatus(ClarifyModeStatusEnum.DONE);
        agentStateStore.save(state.getUserId(), state.getSessionId(), STATE_KEY, ctx);
    }

    @Tool(
            name = CLARIFY_EXIT,
            stateInjected = true,
            description = """
                    Leave CLARIFY mode when you have enough information (including when the request was already clear and no question was needed).
                    Gated MCP tools stay blocked until this succeeds. After success, call the gated tool in this turn.
                    """
    )
    public String exitTool(AgentState state) {
        SystemException.isTrue(Objects.nonNull(state), "Error: agent state unavailable.");
        ClarifyModeContextState ctx =
                getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
        if (ClarifyModeStatusEnum.READY.equals(ctx.getStatus())) {
            return "Error: not in clarify mode. Call " + CLARIFY_ENTER + " first.";
        }
        exit(state);
        return "Clarify mode finished (DONE). You may now call the gated MCP tool using the information collected." +
                "Do not ask more clarifying questions in this turn. On the user's next new request you must " + CLARIFY_ENTER + " again before gated tools.";
    }


    /*public void ask(
            AgentState state,
            String content
    ) {
        ClarifyModeContextState ctx = getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
        if (!ClarifyModeStatusEnum.ACTIVE_STATUS.contains(ctx.getStatus())) {
            throw new IllegalStateException(
                    "Clarify ask status is not running, please call clarify_enter first.");
        }
        ctx.setStatus(ClarifyModeStatusEnum.RUNNING);

        int configured = ctx.getMaxClarifyRounds(); // 0 表示未配置
        int limit = configured > 0
                ? configured
                : aiAppConfig.getDefaultMaxClarifyRounds();
        if (ctx.getQuestions().size() >= limit) {
            throw new IllegalStateException(
                    "Clarify ask limit reached (" + limit + "). Call clarify_exit or proceed with what you have.");
        }
        ctx.getQuestions().add(content);

        agentStateStore.save(state.getUserId(), state.getSessionId(), STATE_KEY, ctx);
    }*/


    /*@Tool(
            name = CLARIFY_ASK,
            description = """
                    Ask the user ONE clarifying question while in clarify mode.
                    Pass only the question text. After this tool succeeds,
                    output that same question to the user in plain chat text.
                    """
    )
    public String askTool(
            AgentState state,
            @ToolParam(
                    name = "question",
                    description = "The single question to ask the user."
            )
            String question
    ) {
        SystemException.isTrue(Objects.nonNull(state), "Error: agent state unavailable.");
        if (StringUtils.isBlank(question)) {
            return "Error: question is required.";
        }
        try {
            ask(state, question);
        } catch (IllegalStateException e) {
            return "Error: " + e.getMessage();
        }
        return "Asked question: " + question;
    }
    */

    private ClarifyModeContextState createClarifyModeContextState(
            String userId,
            String sessionId
    ) {
        ClarifyModeContextState clarifyModeContextState = new ClarifyModeContextState();
        agentStateStore.save(userId, sessionId, STATE_KEY, clarifyModeContextState);
        return clarifyModeContextState;
    }

    @Override
    public void contribute(Toolkit toolkit) {
        toolkit.registerTool(this);
    }
}
