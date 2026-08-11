package io.github.loncra.basic.service.ai.server.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.tool.ToolBase;
import io.agentscope.core.tool.ToolCallParam;
import io.agentscope.core.tool.Toolkit;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.ClarifyModeStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.ClarifyModeContextState;
import io.github.loncra.basic.service.ai.server.service.ClarifyModeManager;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClarifyModeToolContributor implements AgentToolkitContributor {

    public static final String CLARIFY_ENTER = "clarify_enter";

    public static final String CLARIFY_ASK = "clarify_ask";

    public static final String CLARIFY_EXIT = "clarify_exit";

    public static final String CLARIFY_TARGET_NAME_KEY = "clarify_target_name";

    public static final String CLARIFY_TOOL_NAME_KEY = "clarify_tool_name";


    private final ClarifyModeManager clarifyModeManager;

    private final AiMcpPackageService aiMcpPackageService;

    private static ToolResultBlock result(
            ToolCallParam param,
            String text
    ) {
        return ToolResultBlock.text(text)
                .withIdAndName(param.getToolUseBlock().getId(), param.getToolUseBlock().getName());
    }

    private static AgentState stateOf(ToolCallParam param) {
        return RuntimeContext.resolveAgentState(param.getRuntimeContext(), param.getAgent());
    }

    @Override
    public void contribute(Toolkit toolkit) {
        toolkit.registerTool(new ClarifyModeToolContributor.ClarifyEnterTool(clarifyModeManager, aiMcpPackageService));
        toolkit.registerTool(new ClarifyModeToolContributor.ClarifyAskTool(clarifyModeManager));
        toolkit.registerTool(new ClarifyModeToolContributor.ClarifyExitTool(clarifyModeManager));
    }

    public static final class ClarifyEnterTool extends ToolBase {
        private final ClarifyModeManager manager;
        private final AiMcpPackageService aiMcpPackageService;

        public ClarifyEnterTool(
                ClarifyModeManager manager,
                AiMcpPackageService aiMcpPackageService
        ) {
            super(
                    ToolBase.builder()
                            .name(CLARIFY_ENTER)
                            .description(
                                    "Enter CLARIFY mode when the user's request is too vague to call a controlled MCP tool"
                                            + " safely. While clarify mode is active, those tools are blocked."
                                            + " Ask the user one question at a time with "
                                            + CLARIFY_ASK
                                            + ", wait for their typed reply, and only call "
                                            + CLARIFY_EXIT
                                            + " when you have enough information to proceed.")
                            .inputSchema(Map.of(
                                    "type", "object",
                                    "properties", Map.of(
                                            CLARIFY_TARGET_NAME_KEY, Map.of(
                                                    "type", "string",
                                                    "description",
                                                    "MCP client / package / tool-group name that owns the gated tool"
                                                            + " (same as reset_equipped_tools group id, e.g. system_tdx)."
                                            ),
                                            CLARIFY_TOOL_NAME_KEY, Map.of(
                                                    "type", "string",
                                                    "description",
                                                    "Name of the controlled MCP tool you will call after clarify_exit"
                                                            + " (must match a clarify policy toolName, e.g. tdx_wenda_quotes)."
                                            )
                                    ),
                                    "required", List.of(CLARIFY_TARGET_NAME_KEY, CLARIFY_TOOL_NAME_KEY)
                            ))
                            .readOnly(false)
                            .concurrencySafe(false));
            this.manager = manager;
            this.aiMcpPackageService = aiMcpPackageService;
        }

        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            AgentState state = stateOf(param);
            if (state == null) {
                return Mono.just(result(param, "Error: agent state unavailable."));
            }
            String mcpName = Objects.toString(param.getInput().get(CLARIFY_TARGET_NAME_KEY), StringUtils.EMPTY);
            String toolName = Objects.toString(param.getInput().get(CLARIFY_TOOL_NAME_KEY), StringUtils.EMPTY);
            if (StringUtils.isAnyBlank(mcpName, toolName)) {
                return Mono.just(result(param,"Error: clarify_target_name and clarify_tool_name are required."));
            }
            Optional<McpClarifyToolPolicyMetadata> optional = aiMcpPackageService.getMcpClientCacheClarifyToolPolicyMetadata(mcpName, toolName);
            if (optional.isEmpty()) {
                return Mono.just(result(param, "Error: tool not found."));
            }
            ClarifyModeContextState contextState = manager.enter(state, Objects.requireNonNullElse(optional.get().getMaxClarifyRounds(), 0));
            return Mono.just(
                    result(
                            param,
                            "Entered CLARIFY mode ("
                                    + contextState.getStatus()
                                    + "). Do not call controlled MCP tools yet. "
                                    + "Call "
                                    + CLARIFY_ASK
                                    + " with a single question, then show that same question to the user in"
                                    + " plain chat text. After the user replies, ask more only if needed."
                                    + " When enough information is collected, call "
                                    + CLARIFY_EXIT
                                    + "."
                    )
            );
        }
    }

    public static final class ClarifyAskTool extends ToolBase {

        private final ClarifyModeManager manager;

        public ClarifyAskTool(ClarifyModeManager manager) {
            super(
                    ToolBase.builder()
                            .name(CLARIFY_ASK)
                            .description(
                                    "Ask the user ONE clarifying question while in clarify mode. "
                                            + "Pass only the question text. After this tool succeeds, "
                                            + "output that same question to the user in plain chat text."
                            )
                            .inputSchema(
                                    Map.of(
                                            "type", "object",
                                            "properties", Map.of(
                                                    "question", Map.of(
                                                            "type", "string",
                                                            "description", "The single question to ask the user."
                                                    )
                                            ),
                                            "required", List.of("question")
                                    ))
                            .readOnly(false)
                            .concurrencySafe(false));
            this.manager = manager;
        }


        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            AgentState state = stateOf(param);
            if (state == null) {
                return Mono.just(result(param, "Error: agent state unavailable."));
            }
            // 可选：未 enter / 已 DONE 则拒绝
            Object raw = param.getInput().get("question");
            String question = raw == null ? "" : raw.toString().trim();
            if (question.isEmpty()) {
                return Mono.just(result(param, "Error: question is required."));
            }
            try {
                manager.ask(state, question);
            } catch (IllegalStateException e) {
                return Mono.just(result(param, "Error: " + e.getMessage()));
            }
            return Mono.just(result(param, "Asked question: " + question));
        }
    }

    public static final class ClarifyExitTool extends ToolBase {

        private final ClarifyModeManager manager;

        public ClarifyExitTool(ClarifyModeManager manager) {
            super(
                    ToolBase.builder()
                            .name(CLARIFY_EXIT)
                            .description(
                                    "Leave CLARIFY mode after you have enough information from the user. "
                                            + "Controlled MCP tools stay blocked until this succeeds. "
                                            + "Call only when clarifying questions are done.")
                            .inputSchema(Map.of("type", "object", "properties", Map.of()))
                            .readOnly(false)
                            .concurrencySafe(false));
            this.manager = manager;
        }


        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            AgentState state = stateOf(param);
            if (state == null) {
                return Mono.just(result(param, "Error: agent state unavailable."));
            }
            ClarifyModeContextState ctx =
                    manager.getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
            if (ClarifyModeStatusEnum.READY.equals(ctx.getStatus())) {
                return Mono.just(result(param, "Error: not in clarify mode. Call clarify_enter first."));
            }
            manager.exit(state);
            return Mono.just(
                    result(
                            param,
                            "Clarify mode finished (DONE). You may now call controlled MCP tools "
                                    + "using what the user told you. Do not ask more clarifying questions "
                                    + "unless the user starts a new vague request."
                    ));
        }
    }
}
