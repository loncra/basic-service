package io.github.loncra.basic.service.ai.server.resolver.stream;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.loncra.basic.service.ai.server.domain.AssistantMessageStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageRoleEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RStream;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamRangeArgs;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonAgentSseStreamPublishResolver extends AbstractAgentSseStreamPublishResolver {

    public static final String INTERRUPT_TOPIC_PREFIX = "loncra:basic-service:ai:app:agent:sse:interrupt:topic:";
    public static final String INTERRUPT_BUCKET_PREFIX = "loncra:basic-service:ai:app:agent:sse:interrupt:bucket:";
    public static final String INTERRUPT_TOPIC_STOP_MESSAGE = "stop";

    public static final String STREAM_KEY_PREFIX = "loncra:basic-service:ai:app:agent:sse:stream:";

    private static final int STREAM_ID_PARTS_COUNT = 2;

    private final RedissonClient redissonClient;

    @Override
    public void publish(
            String conversationId,
            AbstractAssistantMessageContentMetadata content
    ) {
        RStream<String, String> stream = getStream(conversationId);

        Map<String, String> entry = Map.of(
                RestResult.DEFAULT_DATA_NAME, SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(content))
        );

        StreamMessageId id = stream.add(StreamAddArgs.entries(entry));
        content.setSseEventId(id.toString());
    }

    @Override
    protected String getLastSseEventId(AgentMessageEntity assistant) {
        return Objects.toString(parseStreamMessageId(assistant.getLastSseEventId()));
    }

    @Override
    public void clean(
            String conversationId,
            String streamId
    ) {
        if (log.isDebugEnabled()) {
            log.debug("清除 ID 为 {} 的 最后一个 streamId:{} 范围内容", conversationId, streamId);
        }
        RStream<String, String> stream = getStream(conversationId);
        StreamMessageId endId = parseStreamMessageId(streamId);

        // 查出从最早到指定 streamId（含）的所有条目
        Map<StreamMessageId, Map<String, String>> entries = stream.range(
                StreamRangeArgs.startIdExclusive(StreamMessageId.MIN).endId(endId)
        );

        if (!entries.isEmpty()) {
            stream.remove(entries.keySet().toArray(new StreamMessageId[0]));
        }
    }

    @Override
    public void remove(String conversationId) {
        RStream<String, String> stream = getStream(conversationId);
        stream.expire(getStreamConfig().getRemoveExpireTime().toDuration());
    }

    @Override
    protected List<ServerSentEvent<String>> preOpen(AgentMessageEntity assistant) {
        if (StringUtils.isEmpty(assistant.getLastSseEventId())) {
            return List.of();
        }
        RStream<String, String> stream = getStream(assistant.getAgentConversationId().toString());
        if (!stream.isExists()) {
            return getAgentMessageServerSentEvent(assistant);
        }

        return getAgentAssistantMessageContents(assistant.getLastSseEventId(), stream)
                .stream()
                .map(AbstractAssistantMessageContentMetadata::toServerSentEvent)
                .toList();
    }

    @Override
    protected int compareStreamMessageId(
            String sseLastId,
            String maxInBatch
    ) {
        return sseLastId.compareTo(maxInBatch);
    }

    @Override
    protected List<AbstractAssistantMessageContentMetadata> getStreamContentList(
            String conversationId,
            String lastSseId
    ) {
        RStream<String, String> stream = getStream(conversationId);
        if (!stream.isExists()) {
            return List.of();
        }

        return getAgentAssistantMessageContents(lastSseId, stream);
    }

    private List<AbstractAssistantMessageContentMetadata> getAgentAssistantMessageContents(
            String lastSseId,
            RStream<String, String> stream
    ) {
        StreamMessageId streamMessageId = parseStreamMessageId(lastSseId);
        Map<StreamMessageId, Map<String, String>> ranged = stream.range(
                StreamRangeArgs.startIdExclusive(streamMessageId).endId(StreamMessageId.MAX)
        );

        List<AbstractAssistantMessageContentMetadata> streamData = new LinkedList<>();
        for (Map.Entry<StreamMessageId, Map<String, String>> entry : ranged.entrySet()) {
            String data = entry.getValue().get(RestResult.DEFAULT_DATA_NAME);
            if (StringUtils.isEmpty(data)) {
                continue;
            }
            Map<String, Object> dataMap = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(data, CastUtils.MAP_TYPE_REFERENCE));
            String type = Objects.toString(dataMap.get(TypeIdNameMetadata.TYPE_FIELD_NAME), StringUtils.EMPTY);
            AgentMessageContentTypeEnum typeEnum = ValueEnum.ofEnum(AgentMessageContentTypeEnum.class, type);
            AbstractAssistantMessageContentMetadata messageContent = CastUtils.convertValue(dataMap, typeEnum.getTargetClass());
            messageContent.setSseEventId(entry.getKey().toString());
            streamData.add(messageContent);
        }

        return streamData;
    }

    @Override
    public boolean isCompleted(String conversationId) {
        return !getStream(conversationId).isExists();
    }

    @Override
    public void interrupt(AgentMessageEntity assistant) {
        // 统一用 conversationId：与 RuntimeContext.sessionId = String.valueOf(conversationId) 对齐
        String conversationId = String.valueOf(assistant.getAgentConversationId());
        // 先写 durable 标志：覆盖「chat 已返回、MQ 尚未 execute / listener 未挂上」窗口
        //    TTL 防止异常路径未 clean 导致永久短路下一轮
        redissonClient.getBucket(interruptBucketKey(conversationId))
                .set(Boolean.TRUE, getStreamConfig().getInterruptTimeToLive().toDuration());
        // 再 PUBLISH：若执行节点已订阅，立即唤醒并调 AgentScope.interrupt()
        //    若尚未订阅：消息可能丢，但 ① 的 bucket 会让 onAgent 走「开跑前停止」短路
        redissonClient.getTopic(interruptTopicKey(conversationId)).publish(INTERRUPT_TOPIC_STOP_MESSAGE);
        log.info("已发布停止信号，assistantId={}, conversationId={}",
                assistant.getId(), assistant.getAgentConversationId());
    }

    @Override
    public Object listenerInterrupt(
            Agent agent,
            RuntimeContext ctx,
            Sinks.Many<AgentEvent> stopEventSink
    ) {

        String stopChannel = interruptTopicKey(ctx.getSessionId());
        RTopic topic = redissonClient.getTopic(stopChannel);
        // 用 AtomicInteger 包一层，是为了 clean 时还能拿到 listenerId（你现有 IdValueMetadata 约定）
        AtomicInteger listenerId = new AtomicInteger();
        // 收到任意 stop 消息 → 本地 interrupt(agent...)：推 StopEvent + 调 AgentScope
        int id = topic.addListener(String.class, (channel, msg) -> interrupt(agent, ctx, msg, stopEventSink));
        listenerId.set(id);
        return IdValueMetadata.of(listenerId, topic);
    }

    private String interruptBucketKey(String sessionOrConversationId) {
        // durable 标志：跨 MQ 空窗、跨节点重启后仍可读
        return INTERRUPT_BUCKET_PREFIX + sessionOrConversationId;
    }
    private String interruptTopicKey(String sessionOrConversationId) {
        // Pub/Sub 只负责「已经在跑」的即时唤醒，不保证送达
        return INTERRUPT_TOPIC_PREFIX + sessionOrConversationId;
    }

    private void interrupt(
            Agent agent,
            RuntimeContext ctx,
            String message,
            Sinks.Many<AgentEvent> stopEventSink
    ) {
        if (log.isDebugEnabled()) {
            log.debug("收到停止信号，sessionId={}", ctx.getSessionId());
        }
        if (!INTERRUPT_TOPIC_STOP_MESSAGE.equals(message)) {
            return ;
        }
        AgentMessageEntity assistantMessage = ctx.get(AgentMessageRoleEnum.ASSISTANT.toString());

        // 先把 StopEvent 塞进 sink，走现有 event resolver（写 STOPPED、推 USER_STOP）
        // 要求：middleware 侧 sink 必须已经有订阅者，否则 unicast 会 FAIL_ZERO_SUBSCRIBER
        Sinks.EmitResult emitResult = stopEventSink.tryEmitNext(
                new AssistantMessageStopEvent(
                        assistantMessage.getId(),
                        // replyId：给事件链关联用；与 AgentEnd 的 replyId 不强绑亦可，但建议同一轮用同一值
                        UUID.randomUUID().toString().replace(CastUtils.NEGATIVE, StringUtils.EMPTY)
                )
        );
        if (emitResult.isFailure()) {
            // 集群排查关键：说明订阅时序仍有竞态，或 sink 已终止
            log.warn("StopEvent 未能写入 sink，result={}, sessionId={}", emitResult, ctx.getSessionId());
        }
        // 再调 AgentScope：让后续 LLM chunk 在 checkInterrupted() 处自然收尾 → 触发 AgentEnd
        ReActAgent reactAgent = null;
        if (agent instanceof ReActAgent ra) {
            reactAgent = ra;
        } else if (agent instanceof HarnessAgent ha) {
            reactAgent = ha.getDelegate();
        }
        if (reactAgent != null) {
            reactAgent.interrupt(ctx.getUserId(), ctx.getSessionId());
        } else {
            log.warn("无法获取 ReActAgent，仅发出 StopEvent，上游可能继续跑，agentType={}",
                    agent.getClass().getName());
        }
    }

    @Override
    public void cleanListener(SignalType signalType, RuntimeContext ctx, Object returnValue) {
        IdValueMetadata<AtomicInteger, RTopic> value = CastUtils.cast(returnValue);
        value.getValue().removeListener(value.getId().get());
        redissonClient.getBucket(interruptBucketKey(ctx.getSessionId())).deleteAsync();
    }
    @Override
    public boolean isStreamBeforeInterrupt(RuntimeContext ctx) {
        // 只读用户 interrupt() 留下的标志
        return redissonClient.getBucket(interruptBucketKey(ctx.getSessionId())).isExists();
    }

    protected StreamMessageId parseStreamMessageId(String lastEventId) {
        if (StringUtils.isBlank(lastEventId) || CastUtils.NEGATIVE.equals(lastEventId)) {
            return StreamMessageId.MIN;
        }
        try {
            String[] parts = lastEventId.split(CastUtils.NEGATIVE);
            if (parts.length >= STREAM_ID_PARTS_COUNT) {
                return new StreamMessageId(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
            }
            return StreamMessageId.MIN;
        }
        catch (Exception e) {
            return StreamMessageId.MIN;
        }
    }

    public RStream<String, String> getStream(String conversationId) {
        String key = streamKey(conversationId);
        return redissonClient.getStream(key);
    }

    public static String streamKey(String conversationId) {
        return STREAM_KEY_PREFIX + conversationId;
    }
}
