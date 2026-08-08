package io.github.loncra.basic.service.ai.server.middleware;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.middleware.AgentInput;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.model.ChatUsage;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.domain.AssistantMessageStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockDeltaContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.basic.service.ai.server.utils.ReactorContextUtils;
import io.github.loncra.basic.service.commons.domain.metadata.chat.TextMessageMetadata;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata.MODEL_CALL_START_TIME;
import static io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata.TOKEN_USAGE_KEY;

/**
 * 集群安全的 Agent 停止中间件。
 *
 * @author olale
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterruptSignalMiddleware implements MiddlewareBase {

    private final AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    private final AgentMessageService agentMessageService;

    public static final int CHARS_PER_TOKEN = 4;

    public static final String CHARS_PER_TOKEN_KEY = "charsPerToken";

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
                .concatMap(e -> Flux.deferContextual(ctxView -> ReactorContextUtils.fluxWithContext(ctxView, () -> this.computeTokenUsageIfNotExist(e, ctx))))
                .doFinally(st -> agentSseStreamPublishResolver.cleanListener(st, ctx, returnValue));

    }

    private Flux<AgentEvent> computeTokenUsageIfNotExist(
            AgentEvent currentEvent,
            RuntimeContext ctx
    ) {
        if (!AssistantMessageStopEvent.class.isAssignableFrom(currentEvent.getClass())) {
            return Flux.just(currentEvent);
        }
        AgentMessageEntity assistantMessage = ctx.get(AgentMessageRoleEnum.ASSISTANT.toString());
        if (assistantMessage.getMetadata().containsKey(TOKEN_USAGE_KEY)) {
            return Flux.just(currentEvent);
        }
        AgentMessageEntity userMessage = agentMessageService.get(assistantMessage.getParentId());

        String userInput = TextMessageMetadata.ofString(userMessage.getContent());
        int inputToken = estimateTokens(userInput, userMessage.getModel());

        List<Class<? extends AbstractAssistantMessageContentMetadata>> targetClasses = List.of(
                AgentMessageContentTypeEnum.THINK.getTargetClass(),
                AgentMessageContentTypeEnum.ANSWER.getTargetClass()
        );
        String outputString = assistantMessage.obtainMessageContents()
                .stream()
                .filter(s -> targetClasses.stream().anyMatch(c -> c.isAssignableFrom(s.getClass())))
                .map(AbstractBlockDeltaContentMetadata.class::cast)
                .map(AbstractBlockDeltaContentMetadata::getValue)
                .collect(Collectors.joining());
        int outputToken = estimateTokens(outputString, assistantMessage.getModel());

        Instant creationTime = CastUtils.cast(assistantMessage.getMetadata().getOrDefault(MODEL_CALL_START_TIME, Instant.now()));
        // TODO 这里没带 tool_call 用量计算，如果模型工具执行工具调用到一半的情况，还没考虑清楚要怎么粗略计算。
        ChatUsage chatUsage = ChatUsage.builder()
                .inputTokens(inputToken)
                .outputTokens(outputToken)
                .cachedTokens(0)
                // 对齐官方， toSeconds() 是整秒截断，先不用，按照他的源码是什么就用什么
                .time(Duration.between(creationTime, Instant.now()).toMillis() / 1000.0)
                .build();

        return Flux.just(new ModelCallEndEvent(newReplyId(),chatUsage), currentEvent);
    }

    public static int estimateTokens(String text, ModelSettingMetadata metadata) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        int charsPerToken = (int) metadata.getMetadata().getOrDefault(CHARS_PER_TOKEN_KEY, CHARS_PER_TOKEN);
        // 至少输出过一点内容时给 1，避免全 0 看不出停过
        int tokens = (int) Math.ceil(text.length() / (double) charsPerToken);
        return Math.max(tokens, text.isEmpty() ? 0 : 1);
    }

    private String newReplyId() {
        return UUID.randomUUID().toString().replace(CastUtils.NEGATIVE, StringUtils.EMPTY);
    }
}
