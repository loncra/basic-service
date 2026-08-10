package io.github.loncra.basic.service.ai.server.middleware;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultStartEvent;
import io.agentscope.core.event.ToolResultTextDeltaEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolResultState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.middleware.ActingInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.state.AgentState;
import io.agentscope.core.tool.ToolResultMessageBuilder;
import io.github.loncra.basic.service.ai.server.constants.ClarifyConstants;
import io.github.loncra.basic.service.ai.server.service.clarify.ClarifyModeManager;
import io.github.loncra.basic.service.ai.server.service.clarify.ClarifyPolicyResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Clarify Mode 中间件：对齐 PlanModeMiddleware，在 onActing 拦截受控 MCP。
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class ClarifyMiddleware implements MiddlewareBase {

    private static final Set<String> CLARIFY_WHITELIST = Set.of(
            ClarifyConstants.CLARIFY_ENTER,
            ClarifyConstants.CLARIFY_WRITE,
            ClarifyConstants.CLARIFY_EXIT
    );

    private final ClarifyModeManager clarifyModeManager;

    private final ClarifyPolicyResolver clarifyPolicyResolver;

    @Override
    public Flux<AgentEvent> onActing(
            Agent agent,
            RuntimeContext ctx,
            ActingInput input,
            Function<ActingInput, Flux<AgentEvent>> next
    ) {
        if (input == null || input.toolCalls() == null || input.toolCalls().isEmpty()) {
            return next.apply(input);
        }
        AgentState state = RuntimeContext.resolveAgentState(ctx, agent);
        String sessionId = state == null ? ctx.getSessionId() : state.getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            return next.apply(input);
        }

        boolean clarifyActive = clarifyModeManager.isActive(sessionId);
        List<ToolUseBlock> allowed = new ArrayList<>();
        List<ToolUseBlock> denied = new ArrayList<>();

        for (ToolUseBlock toolCall : input.toolCalls()) {
            String name = toolCall.getName();
            if (CLARIFY_WHITELIST.contains(name)) {
                allowed.add(toolCall);
                continue;
            }
            boolean controlled = clarifyPolicyResolver.isControlledTool(name);
            if (clarifyActive) {
                denied.add(toolCall);
                continue;
            }
            if (controlled) {
                if (clarifyModeManager.canExecuteControlledTool(sessionId, name)) {
                    allowed.add(toolCall);
                    clarifyModeManager.incrementCallCount(sessionId, name);
                } else if (clarifyModeManager.isBudgetExceeded(sessionId, name)) {
                    denied.add(toolCall);
                } else {
                    // 未澄清：自动进入 clarify，并拒绝本次 MCP
                    clarifyModeManager.enter(sessionId, name);
                    denied.add(toolCall);
                }
                continue;
            }
            allowed.add(toolCall);
        }

        if (denied.isEmpty()) {
            return next.apply(new ActingInput(allowed));
        }

        String replyId = state == null || StringUtils.isBlank(state.getReplyId())
                ? "clarify-deny"
                : state.getReplyId();
        Flux<AgentEvent> denyFlux = Flux.fromIterable(buildDeniedEvents(denied, replyId, sessionId, state));
        if (allowed.isEmpty()) {
            return denyFlux;
        }
        return Flux.concat(denyFlux, next.apply(new ActingInput(allowed)));
    }

    private List<AgentEvent> buildDeniedEvents(
            List<ToolUseBlock> denied,
            String replyId,
            String sessionId,
            AgentState state
    ) {
        List<AgentEvent> events = new LinkedList<>();
        for (ToolUseBlock toolCall : denied) {
            String message = resolveDenyMessage(sessionId, toolCall.getName());
            ToolResultBlock block = ToolResultBlock.text(message)
                    .withIdAndName(toolCall.getId(), toolCall.getName())
                    .withState(ToolResultState.DENIED);
            if (state != null) {
                Msg msg = ToolResultMessageBuilder.buildToolResultMsg(block, toolCall, state.getSessionId());
                state.contextMutable().add(msg);
            }
            events.add(new ToolResultStartEvent(replyId, toolCall.getId(), toolCall.getName()));
            events.add(new ToolResultTextDeltaEvent(replyId, toolCall.getId(), toolCall.getName(), message));
            events.add(new ToolResultEndEvent(replyId, toolCall.getId(), toolCall.getName(), ToolResultState.DENIED));
        }
        return events;
    }

    private String resolveDenyMessage(String sessionId, String toolName) {
        if (clarifyModeManager.isActive(sessionId)) {
            return ClarifyConstants.DENY_CLARIFY_WHITELIST;
        }
        if (clarifyModeManager.isBudgetExceeded(sessionId, toolName)) {
            return ClarifyConstants.DENY_BUDGET_EXCEEDED;
        }
        return ClarifyConstants.DENY_NEED_CLARIFY;
    }
}
