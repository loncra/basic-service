package io.github.loncra.basic.service.ai.server.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionDecision;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.tool.ToolBase;
import io.agentscope.core.tool.ToolCallParam;
import io.github.loncra.basic.service.ai.server.constants.ClarifyConstants;
import io.github.loncra.basic.service.ai.server.domain.clarify.ClarifyToolPolicy;
import io.github.loncra.basic.service.ai.server.service.clarify.ClarifyModeManager;
import io.github.loncra.basic.service.ai.server.service.clarify.ClarifyPolicyResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Clarify Mode 工具组，对齐 {@code PlanModeTools} 的 enter / write / exit 形态。
 */
@Component
@RequiredArgsConstructor
public class ClarifyModeTools {

    private final ClarifyModeManager clarifyModeManager;

    private final ClarifyPolicyResolver clarifyPolicyResolver;

    public ClarifyEnterTool enterTool() {
        return new ClarifyEnterTool(clarifyModeManager);
    }

    public ClarifyWriteTool writeTool() {
        return new ClarifyWriteTool(clarifyModeManager, clarifyPolicyResolver);
    }

    public ClarifyExitTool exitTool() {
        return new ClarifyExitTool(clarifyModeManager);
    }

    private static String sessionIdOf(ToolCallParam param) {
        AgentState state = RuntimeContext.resolveAgentState(param.getRuntimeContext(), param.getAgent());
        if (state == null || StringUtils.isBlank(state.getSessionId())) {
            return null;
        }
        return state.getSessionId();
    }

    private static ToolResultBlock result(ToolCallParam param, String text) {
        return ToolResultBlock.text(text)
                .withIdAndName(param.getToolUseBlock().getId(), param.getToolUseBlock().getName());
    }

    public static final class ClarifyEnterTool extends ToolBase {

        private final ClarifyModeManager manager;

        public ClarifyEnterTool(ClarifyModeManager manager) {
            super(ToolBase.builder()
                    .name(ClarifyConstants.CLARIFY_ENTER)
                    .description("Enter clarify mode before calling a controlled MCP tool. "
                            + "Use when user intent is ambiguous. After enter, call clarify_write with A2UI commands, then clarify_exit.")
                    .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    ClarifyConstants.TOOL_PARAM_TARGET_TOOL, Map.of(
                                            "type", "string",
                                            "description", "Controlled MCP tool name to clarify for, e.g. tdx_wenda_quotes"
                                    )
                            )
                    ))
                    .readOnly(true)
                    .concurrencySafe(true));
            this.manager = manager;
        }

        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            String sessionId = sessionIdOf(param);
            if (StringUtils.isBlank(sessionId)) {
                return Mono.just(result(param, "Error: session unavailable for clarify_enter."));
            }
            Map<String, Object> input = param.getInput() == null ? Map.of() : param.getInput();
            String targetTool = ObjectsToString(input.get(ClarifyConstants.TOOL_PARAM_TARGET_TOOL));
            manager.enter(sessionId, targetTool);
            return Mono.just(result(param, "Clarify mode active. Next call clarify_write with content.commands "
                    + "(A2UI v0.9: TextField/SelectField/CheckboxField/ActionButton; no textarea), then clarify_exit."));
        }
    }

    public static final class ClarifyWriteTool extends ToolBase {

        private final ClarifyModeManager manager;

        private final ClarifyPolicyResolver policyResolver;

        public ClarifyWriteTool(ClarifyModeManager manager, ClarifyPolicyResolver policyResolver) {
            super(ToolBase.builder()
                    .name(ClarifyConstants.CLARIFY_WRITE)
                    .description("Write the full clarify UI as A2UI v0.9 commands for XCardBox "
                            + "(like plan_write writes full plan markdown). "
                            + "content.commands REQUIRED: createSurface + updateComponents + updateDataModel. "
                            + "Use TextField/SelectField/CheckboxField/Panel/Text/ActionButton only. "
                            + "Do NOT use textarea. Required dimension keys must appear as dataModel paths "
                            + "(e.g. /subject). ActionButton submit event name must be clarify.submit; cancel clarify.cancel.")
                    .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    ClarifyConstants.TOOL_PARAM_TARGET_TOOL, Map.of(
                                            "type", "string",
                                            "description", "Controlled MCP tool name"
                                    ),
                                    ClarifyConstants.TOOL_PARAM_CONTENT, Map.of(
                                            "type", "object",
                                            "description", "Clarify card: { commands: XCardCommand[], title? }. "
                                                    + "commands is A2UI v0.9 for XCardBox."
                                    )
                            ),
                            "required", java.util.List.of(ClarifyConstants.TOOL_PARAM_CONTENT)
                    ))
                    .readOnly(true)
                    .concurrencySafe(false));
            this.manager = manager;
            this.policyResolver = policyResolver;
        }

        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            String sessionId = sessionIdOf(param);
            if (StringUtils.isBlank(sessionId)) {
                return Mono.just(result(param, "Error: session unavailable for clarify_write."));
            }
            Map<String, Object> input = param.getInput() == null ? Map.of() : param.getInput();
            String targetTool = ObjectsToString(input.get(ClarifyConstants.TOOL_PARAM_TARGET_TOOL));
            Map<String, Object> content = CastUtils.convertValue(
                    input.get(ClarifyConstants.TOOL_PARAM_CONTENT),
                    CastUtils.MAP_TYPE_REFERENCE
            );
            if (MapUtils.isEmpty(content)) {
                return Mono.just(result(param, "Error: content is required for clarify_write."));
            }
            if (StringUtils.isBlank(targetTool)) {
                targetTool = manager.getOrCreate(sessionId).getTargetTool();
            }
            if (StringUtils.isBlank(targetTool)) {
                return Mono.just(result(param, "Error: targetTool is required."));
            }
            Optional<ClarifyToolPolicy> policy = policyResolver.findToolPolicy(targetTool);
            if (policy.isEmpty()) {
                return Mono.just(result(param, "Error: tool [" + targetTool + "] is not under clarify policy."));
            }
            try {
                if (content.get(ClarifyConstants.CARD_COMMANDS) == null
                        && MapUtils.isNotEmpty(policy.get().getFormTemplate())) {
                    Map<String, Object> merged = new LinkedHashMap<>(policy.get().getFormTemplate());
                    merged.putAll(content);
                    content = merged;
                }
                manager.validateCardAgainstPolicy(targetTool, content);
                manager.writeCard(sessionId, targetTool, content);
            } catch (RuntimeException ex) {
                return Mono.just(result(param, "Error: " + ex.getMessage()));
            }
            return Mono.just(result(param, "Clarify A2UI commands written for tool [" + targetTool
                    + "]. Call clarify_exit with a short summary for user approval."));
        }
    }

    public static final class ClarifyExitTool extends ToolBase {

        private final ClarifyModeManager manager;

        public ClarifyExitTool(ClarifyModeManager manager) {
            super(ToolBase.builder()
                    .name(ClarifyConstants.CLARIFY_EXIT)
                    .description("Finish clarify drafting and request user approval (like plan_exit). "
                            + "Pauses for the user to fill/submit the clarify form. "
                            + "summary is a short rationale shown to the user.")
                    .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    ClarifyConstants.TOOL_PARAM_SUMMARY, Map.of(
                                            "type", "string",
                                            "description", "Optional short summary for user approval"
                                    )
                            )
                    ))
                    .readOnly(false)
                    .concurrencySafe(false));
            this.manager = manager;
        }

        @Override
        public Mono<PermissionDecision> checkPermissions(
                Map<String, Object> input,
                PermissionContextState permissionContext
        ) {
            String sessionId = trySessionId(permissionContext);
            if (StringUtils.isNotBlank(sessionId) && !manager.isWriteDone(sessionId)) {
                return Mono.just(PermissionDecision.deny(ClarifyConstants.ERR_EXIT_NEED_WRITE));
            }
            return Mono.just(PermissionDecision.ask(
                    "The agent prepared a clarify A2UI form. User fills it in the answer card, then approve or reject."
            ));
        }

        private static String trySessionId(PermissionContextState permissionContext) {
            if (permissionContext == null) {
                return null;
            }
            for (String methodName : new String[]{"getSessionId", "sessionId"}) {
                try {
                    Object value = permissionContext.getClass().getMethod(methodName).invoke(permissionContext);
                    if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                        return String.valueOf(value);
                    }
                } catch (ReflectiveOperationException ignored) {
                    // continue
                }
            }
            try {
                Object agentState = permissionContext.getClass().getMethod("getAgentState").invoke(permissionContext);
                if (agentState instanceof AgentState state && StringUtils.isNotBlank(state.getSessionId())) {
                    return state.getSessionId();
                }
            } catch (ReflectiveOperationException ignored) {
                // ignore
            }
            return null;
        }

        @Override
        public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
            String sessionId = sessionIdOf(param);
            if (StringUtils.isBlank(sessionId)) {
                return Mono.just(result(param, "Error: session unavailable for clarify_exit."));
            }
            try {
                // 用户已提交后 active 可能为 false；只校验 writeDone
                manager.markExitPending(sessionId);
            } catch (RuntimeException ex) {
                return Mono.just(result(param, "Error: " + ex.getMessage()));
            }
            Map<String, Object> input = param.getInput() == null ? Map.of() : param.getInput();
            String summary = ObjectsToString(input.get(ClarifyConstants.TOOL_PARAM_SUMMARY));
            String msg = StringUtils.isBlank(summary)
                    ? "Clarify form ready. Waiting for user submission."
                    : "Clarify form ready: " + summary;
            return Mono.just(result(param, msg));
        }
    }

    private static String ObjectsToString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
