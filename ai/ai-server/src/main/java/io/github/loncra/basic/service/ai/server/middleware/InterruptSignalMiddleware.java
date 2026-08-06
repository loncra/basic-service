package io.github.loncra.basic.service.ai.server.middleware;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.github.loncra.basic.service.ai.server.domain.AssistantMessageStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * 集群安全的 Agent 停止中间件。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterruptSignalMiddleware implements MiddlewareBase {

    private final AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    @Override
    public Flux<AgentEvent> onAgent(
            Agent agent,
            RuntimeContext ctx,
            AgentInput input,
            Function<AgentInput, Flux<AgentEvent>> next
    ) {

        // 缓存最近 1 条 Stop：Redis 回调早于外层 subscribe 时，晚订阅仍能拿到
        Sinks.Many<AgentEvent> stopEventSink = Sinks.many().replay().limit(1);
        Flux<AgentEvent> stopFlux = stopEventSink.asFlux();
        Object returnValue = agentSseStreamPublishResolver.listenerInterrupt(agent, ctx, stopEventSink);
        // ---------- 开跑前已 interrupt：不跑模型，手工 Stop → End ----------
        if (agentSseStreamPublishResolver.isStreamBeforeInterrupt(ctx)) {
            String replyId = newReplyId();
            AgentMessageEntity message = ctx.get(AgentMessageRoleEnum.ASSISTANT.toString());
            AssistantMessageStopEvent stopEvent = new AssistantMessageStopEvent(message.getId(), replyId);
            // 与你现有构造方式保持一致
            AgentEndEvent agentEndEvent = new AgentEndEvent(replyId);
            if (log.isDebugEnabled()) {
                log.debug("用户已停止，跳过推流与模型执行，sessionId={}", ctx.getSessionId());
            }
            agentSseStreamPublishResolver.cleanListener(SignalType.CANCEL, ctx, returnValue);
            return Flux.just(stopEvent, agentEndEvent);
        }
        if (log.isDebugEnabled()) {
            log.debug("已注册停止监听器，sessionId={}", ctx.getSessionId());
        }

        Flux<AgentEvent> oneStop = stopFlux.take(1);
        Flux<AgentEvent> agentFlux = next.apply(input)
                .doFinally(signalType -> stopEventSink.tryEmitComplete());
        return Flux.merge(agentFlux, oneStop)
                .doFinally(st -> agentSseStreamPublishResolver.cleanListener(st, ctx, returnValue));

    }

    private @NonNull Flux<AgentEndEvent> replyEvent(
            RuntimeContext ctx,
            AtomicBoolean userStopped
    ) {
        if (!userStopped.get()) {
            return Flux.empty();
        }
        String replyId = newReplyId();
        if (log.isDebugEnabled()) {
            log.debug("用户停止后补发 AgentEnd，sessionId={}, replyId={}", ctx.getSessionId(), replyId);
        }
        return Flux.just(new AgentEndEvent(replyId));
    }

    private String newReplyId() {
        return UUID.randomUUID().toString().replace(CastUtils.NEGATIVE, StringUtils.EMPTY);
    }
}
