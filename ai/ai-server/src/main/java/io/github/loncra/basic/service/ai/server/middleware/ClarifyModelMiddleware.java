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
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.ClarifyModeStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.ClarifyModeContextState;
import io.github.loncra.basic.service.ai.server.service.ClarifyModeManager;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ClarifyModelMiddleware implements MiddlewareBase {

    private final ClarifyModeManager manager;

    private final AiMcpPackageService aiMcpPackageService;

    private static final String DENY_MESSAGE = "Blocked: gated tool requires " + ClarifyModeManager.CLARIFY_EXIT + " first."
            + "If not in clarify mode, call " + ClarifyModeManager.CLARIFY_ENTER + " (" + ClarifyModeManager.CLARIFY_MCP_NAME_KEY + ", " + ClarifyModeManager.CLARIFY_TOOL_NAME_KEY + ")."
            + "If clarifying, ask ONE A / B / C (and more if needed) question in plain chat, wait for the user, then " + ClarifyModeManager.CLARIFY_EXIT + ", then retry this tool.";

    @Override
    public Flux<AgentEvent> onActing(
            Agent agent,
            RuntimeContext ctx,
            ActingInput input,
            Function<ActingInput, Flux<AgentEvent>> next
    ) {

        if (Objects.isNull(input) || CollectionUtils.isEmpty(input.toolCalls())) {
            return next.apply(input);
        }

        List<McpClarifyToolPolicyMetadata> policies = aiMcpPackageService.findMcpClientCacheClarifyToolPolicyMetadata();
        if (CollectionUtils.isEmpty(policies)) {
            return next.apply(input);
        }

        ClarifyModeContextState clarifyModeContextState = manager.getOrCreateClarifyModeContextState(ctx.getUserId(), ctx.getSessionId());
        if (!ClarifyModeStatusEnum.ACTIVE_STATUS.contains(clarifyModeContextState.getStatus())) {
            return next.apply(input);
        }

        AgentState state = RuntimeContext.resolveAgentState(ctx, agent);

        List<ToolUseBlock> allowed = new ArrayList<>();
        List<ToolUseBlock> denied = new ArrayList<>();
        for (ToolUseBlock call : input.toolCalls()) {
            if (policies.stream().noneMatch(s -> Strings.CS.equals(s.getToolName(), call.getName()))) {
                allowed.add(call);
            } else {
                denied.add(call);
            }
        }

        if (denied.isEmpty()) {
            return next.apply(input);
        }

        Flux<AgentEvent> deniedFlux = Flux.defer(() -> this.createDeniedFlux(denied, state, agent));
        if (allowed.isEmpty()) {
            return deniedFlux;
        }

        return deniedFlux.concatWith(next.apply(new ActingInput(allowed)));
    }

    private Flux<AgentEvent> createDeniedFlux(
            List<ToolUseBlock> denied,
            AgentState state,
            Agent agent
    ) {
        String replyId = state.getReplyId();
        List<AgentEvent> events = new ArrayList<>();
        for (ToolUseBlock call : denied) {
            ToolResultBlock result = ToolResultBlock.text(DENY_MESSAGE)
                    .withIdAndName(call.getId(), call.getName())
                    .withState(ToolResultState.DENIED);
            Msg msg = ToolResultMessageBuilder.buildToolResultMsg(result, call, agent.getName());
            state.contextMutable().add(msg);
            events.add(new ToolResultStartEvent(replyId, call.getId(), call.getName()));
            events.add(new ToolResultTextDeltaEvent(replyId, call.getId(), call.getName(), DENY_MESSAGE));
            events.add(new ToolResultEndEvent(replyId, call.getId(), call.getName(), ToolResultState.DENIED));
        }
        return Flux.fromIterable(events);
    }

}
