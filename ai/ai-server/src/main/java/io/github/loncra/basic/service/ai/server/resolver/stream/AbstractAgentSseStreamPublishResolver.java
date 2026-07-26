package io.github.loncra.basic.service.ai.server.resolver.stream;

import io.github.loncra.basic.service.ai.server.config.StreamConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentSseStreamPublishResolver;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.exception.ErrorCodeException;
import io.github.loncra.framework.commons.exception.ServiceException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Setter(onMethod_ = @Autowired)
public abstract class AbstractAgentSseStreamPublishResolver implements AgentSseStreamPublishResolver {

    private StreamConfig streamConfig;

    public void publish(
            String conversationId,
            AgentAssistantMessageContent content
    ) {
        doPublish(conversationId, content);
        if (AgentMessageContentTypeEnum.COMPLETED_STATUS.contains(content.getType())) {
            remove(conversationId);
        }
    }

    protected abstract void doPublish(
            String conversationId,
            AgentAssistantMessageContent content
    );

    @Override
    public Flux<ServerSentEvent<String>> open(AgentMessageEntity assistant) {

        Flux<ServerSentEvent<String>> preFromFlux = Flux.fromIterable(preOpen(assistant));
        Flux<ServerSentEvent<String>> liveFlux = Flux.create(sink -> pollStreamFrom(sink, assistant));
        return Flux.concat(preFromFlux, liveFlux);
    }

    private void pollStreamFrom(
            FluxSink<ServerSentEvent<String>> sink,
            AgentMessageEntity assistant
    ) {
        AtomicReference<String> lastSseId = new AtomicReference<>(getLastSseEventId(assistant));
        AtomicReference<Disposable> nextPollRef = new AtomicReference<>();
        sink.onDispose(() -> closeDisposableIfNotNull(nextPollRef));
        scheduleNextPoll(sink, assistant.getAgentConversationId().toString(), lastSseId, nextPollRef);
    }

    protected abstract String getLastSseEventId(AgentMessageEntity assistant);

    /**
     * 取出并 dispose 引用中的 Disposable（用于客户端断开时取消下次轮询）。
     *
     * @param nextPollRef 持有「下次轮询」Disposable 的引用，取后置为 null
     */
    private void closeDisposableIfNotNull(AtomicReference<Disposable> nextPollRef) {
        Disposable d = nextPollRef.getAndSet(null);
        if (d == null) {
            return;
        }

        d.dispose();
    }

    protected List<ServerSentEvent<String>> preOpen(AgentMessageEntity assistant) {
        return List.of();
    }

    /**
     * 在 POLL_INTERVAL 后调度执行一次 pollStreamOnce，并将返回的 Disposable 存入 nextPollRef 以便 onDispose 时取消。
     *
     * @param sink        SSE 流的 sink
     * @param sessionId   会话 id
     * @param sseLastId      当前已推送给客户端的最大 StreamMessageId
     * @param nextPollRef 用于存放本次调度的 Disposable，客户端断开时 dispose
     */
    private void scheduleNextPoll(
            FluxSink<ServerSentEvent<String>> sink,
            String sessionId,
            AtomicReference<String> sseLastId,
            AtomicReference<Disposable> nextPollRef
    ) {

        TimeProperties pollInterval = streamConfig.getPollInterval();
        Disposable d;
        if (Objects.nonNull(pollInterval)) {
            d = Schedulers.boundedElastic()
                    .schedule(() -> pollStreamOnce(sink, sessionId, sseLastId, nextPollRef), pollInterval.toMillis(), pollInterval.getUnit());
        }
        else {
            d = Schedulers.boundedElastic()
                    .schedule(() -> pollStreamOnce(sink, sessionId, sseLastId, nextPollRef));
        }
        nextPollRef.set(d);
    }

    /**
     * 执行一次轮询：从 Redis Stream 读取 lastId 之后的新事件，推给 sink；若有新事件则更新 lastId 并 scheduleNextPoll，收到 COMPLETED/STOPPED/ERROR 则 complete。
     *
     * @param sink        SSE 流的 sink
     * @param conversationId    会话 id
     * @param sseLastId      当前已推送给客户端的最大 StreamMessageId，本方法会更新
     * @param nextPollRef 用于 scheduleNextPoll 时写入 Disposable
     */
    private void pollStreamOnce(
            FluxSink<ServerSentEvent<String>> sink,
            String conversationId,
            AtomicReference<String> sseLastId,
            AtomicReference<Disposable> nextPollRef
    ) {
        if (sink.isCancelled()) {
            return;
        }

        try {
            if (isCompleted(conversationId)) {
                sink.complete();
                return;
            }

            List<AgentAssistantMessageContent> batch = getStreamContentList(conversationId, sseLastId.get());
            if (CollectionUtils.isEmpty(batch)) {
                scheduleNextPoll(sink, conversationId, sseLastId, nextPollRef);
                return;
            }
            String currentLast = sseLastId.get();
            String maxInBatch = currentLast;
            for (AgentAssistantMessageContent content : batch) {
                if (sink.isCancelled()) {
                    return;
                }

                if (compareStreamMessageId(content.getSseEventId(), currentLast) <= 0) {
                    continue;
                }

                if (compareStreamMessageId(content.getSseEventId(), maxInBatch) > 0) {
                    maxInBatch = content.getSseEventId();
                }

                ServerSentEvent<String> serverSentEvent = content.toServerSentEvent();
                sink.next(serverSentEvent);
                if (AgentMessageContentTypeEnum.COMPLETED.getValue().equals(serverSentEvent.event())) {
                    sink.complete();
                    return;
                }
                else if (AgentMessageContentTypeEnum.ERROR.getValue().equals(serverSentEvent.event()) && Objects.nonNull(serverSentEvent.data())) {
                    String error = StringUtils.defaultIfEmpty(serverSentEvent.data(), ErrorCodeException.DEFAULT_ERROR_MESSAGE);
                    sink.error(new ServiceException(error));
                    return;
                }
            }
            sseLastId.set(maxInBatch);
            scheduleNextPoll(sink, conversationId, sseLastId, nextPollRef);
        }
        catch (Exception e) {
            log.error("推流出现异常", e);
            sink.error(e);
        }
    }

    protected abstract int compareStreamMessageId(
            String sseEventId,
            String maxInBatch
    );

    protected abstract List<AgentAssistantMessageContent> getStreamContentList(String conversationId, String lastSseId);
}
