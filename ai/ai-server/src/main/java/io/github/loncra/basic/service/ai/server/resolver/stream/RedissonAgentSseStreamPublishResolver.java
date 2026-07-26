package io.github.loncra.basic.service.ai.server.resolver.stream;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.api.stream.StreamRangeArgs;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RedissonAgentSseStreamPublishResolver extends AbstractAgentSseStreamPublishResolver {

    public static final String KEY_PREFIX = "loncra:basic-service:ai:app:agent:sse:stream:";

    private static final int STREAM_ID_PARTS_COUNT = 2;

    private final RedissonClient redissonClient;

    @Override
    public void doPublish(
            String conversationId,
            AgentAssistantMessageContent content
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

    }

    @Override
    public void remove(String conversationId) {
        RStream<String, String> stream = getStream(conversationId);
        stream.expire(Duration.ofSeconds(5));
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
                .map(AgentAssistantMessageContent::toServerSentEvent)
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
    protected List<AgentAssistantMessageContent> getStreamContentList(
            String conversationId,
            String lastSseId
    ) {
        RStream<String, String> stream = getStream(conversationId);
        if (!stream.isExists()) {
            return List.of();
        }

        return getAgentAssistantMessageContents(lastSseId, stream);
    }

    private List<AgentAssistantMessageContent> getAgentAssistantMessageContents(
            String lastSseId,
            RStream<String, String> stream
    ) {
        StreamMessageId streamMessageId = parseStreamMessageId(lastSseId);
        Map<StreamMessageId, Map<String, String>> ranged = stream.range(
                StreamRangeArgs.startIdExclusive(streamMessageId).endId(StreamMessageId.MAX)
        );

        List<AgentAssistantMessageContent> streamData = new LinkedList<>();
        for (Map.Entry<StreamMessageId, Map<String, String>> entry : ranged.entrySet()) {
            String data = entry.getValue().get(RestResult.DEFAULT_DATA_NAME);
            if (StringUtils.isEmpty(data)) {
                continue;
            }
            Map<String, Object> dataMap = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(data, CastUtils.MAP_TYPE_REFERENCE));
            String type = Objects.toString(dataMap.get(TypeIdNameMetadata.TYPE_FIELD_NAME), StringUtils.EMPTY);
            AgentMessageContentTypeEnum typeEnum = ValueEnum.ofEnum(AgentMessageContentTypeEnum.class, type);
            AgentAssistantMessageContent messageContent = CastUtils.convertValue(dataMap, typeEnum.getTargetClass());
            messageContent.setSseEventId(entry.getKey().toString());
            streamData.add(messageContent);
        }

        return streamData;
    }

    @Override
    protected boolean isCompleted(String conversationId) {
        return !getStream(conversationId).isExists();
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
        return KEY_PREFIX + conversationId;
    }
}
