package io.github.loncra.basic.service.ai.server.service;

import io.agentscope.core.state.AgentState;
import io.agentscope.core.state.AgentStateStore;
import io.github.loncra.basic.service.ai.api.enumerate.ClarifyModeStatusEnum;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.domain.ClarifyModeContextState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
@RequiredArgsConstructor
public class ClarifyModeManager {

    public static final String STATE_KEY = "clarify_mode";

    private final AgentStateStore agentStateStore;

    private final AiAppConfig aiAppConfig;

    public ClarifyModeContextState getOrCreateClarifyModeContextState(
            String userId,
            String sessionId
    ) {
        return agentStateStore.get(userId, sessionId, STATE_KEY, ClarifyModeContextState.class)
                .orElseGet(() -> createClarifyModeContextState(userId, sessionId));
    }

    public ClarifyModeContextState enter(AgentState state, int maxClarifyRounds) {

        ClarifyModeContextState ctx = getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
        ctx.setStatus(ClarifyModeStatusEnum.PENDING);
        ctx.setMaxClarifyRounds(maxClarifyRounds);
        ctx.setQuestions(new LinkedList<>());

        agentStateStore.save(state.getUserId(), state.getSessionId(), STATE_KEY, ctx);
        return ctx;
    }

    /** Exits plan mode (back to BUILD). Idempotent. Keeps {@code currentPlanFile} for reference. */
    public void exit(AgentState state) {
        ClarifyModeContextState ctx = getOrCreateClarifyModeContextState(state.getUserId(), state.getSessionId());
        ctx.setStatus(ClarifyModeStatusEnum.DONE);
        agentStateStore.save(state.getUserId(), state.getSessionId(), STATE_KEY, ctx);
    }

    public void ask(AgentState state, String content) {
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
    }

    private ClarifyModeContextState createClarifyModeContextState(
            String userId,
            String sessionId
    ) {
        ClarifyModeContextState clarifyModeContextState = new ClarifyModeContextState();
        agentStateStore.save(userId, sessionId, STATE_KEY, clarifyModeContextState);
        return clarifyModeContextState;
    }

}
