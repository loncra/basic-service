package io.github.loncra.basic.service.message.server.event;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallEntity;
import io.github.loncra.basic.service.message.server.domain.entity.chat.call.UserChatCallParticipantEntity;
import io.github.loncra.basic.service.message.server.enumerate.chat.call.UserChatCallParticipantStatusEnum;
import io.github.loncra.basic.service.message.server.service.chat.call.UserChatCallManager;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.idempotent.annotation.Concurrent;
import io.github.loncra.framework.socketio.api.metadata.AbstractSocketMessageMetadata;
import io.github.loncra.framework.socketio.api.metadata.BroadcastMessageMetadata;
import io.github.loncra.framework.socketio.core.holder.SocketResultHolder;
import io.github.loncra.framework.socketio.core.holder.annotation.SocketMessage;
import livekit.LivekitModels;
import livekit.LivekitWebhook;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKitMediaServerWebhookEventListener {

    public static final String DELAY_MAP_CACHE_KEY = "loncra:basic-service:message:app:chat:call:delay";

    public static final String DELAY_CONCURRENT_KEY = "loncra:basic-service:message:app:chat:call:delay:expired-listener:concurrent:";

    private final UserChatCallManager userChatCallManager;

    @Getter
    private final RedissonClient redissonClient;

    @SocketMessage
    @Transactional(rollbackFor = Exception.class)
    public void delayCompleted(LivekitWebhook.WebhookEvent event) {
        Long userChatCallId = getUserChatCallId(event.getRoom().getName());
        if (LivekitModels.DisconnectReason.CLIENT_INITIATED.equals(event.getParticipant().getDisconnectReason())) {
            TimeProperties timeProperties = userChatCallManager.getUserChatCallConfig().getDelayCompletedTime();
            RMapCache<Long, String> mapCache = getMapCache();
            mapCache.put(userChatCallId, event.getParticipant().getIdentity(), timeProperties.getValue(), timeProperties.getUnit());

            UserChatCallParticipantEntity participant = userChatCallManager.getUserChatCallParticipantService()
                    .getByUserChatCallIdAndPrincipal(userChatCallId, event.getParticipant().getIdentity());
            participant.setLeaveTime(Instant.now());
            participant.setStatus(UserChatCallParticipantStatusEnum.DIS_CONNECTION);
            participant.setReconnectTime(participant.getLeaveTime().plus(timeProperties.toDuration()));

            BroadcastMessageMetadata<UserChatCallParticipantEntity> metadata = BroadcastMessageMetadata.of(
                    event.getRoom().getName(),
                    UserChatCallManager.CHAT_CALL_PARTICIPANT_UPDATE_EVENT_NAME,
                    participant
            );

            userChatCallManager.getUserChatCallParticipantService().updateById(participant);

            SocketResultHolder.get().getMessages().add(metadata);
        } else {
            completedChatCall(userChatCallId, event.getParticipant().getIdentity());
        }
    }

    @SocketMessage
    @Concurrent(DELAY_CONCURRENT_KEY + "[#userChatCallId]")
    public void completedChatCall(
            Long userChatCallId,
            String identity
    ) {
        List<AbstractSocketMessageMetadata<Object>> messages = userChatCallManager.completed(userChatCallId, identity);
        SocketResultHolder.get().getMessages().addAll(messages);
    }

    @SocketMessage
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void webhookEvent(LivekitWebhook.WebhookEvent event) throws InvalidProtocolBufferException {
        if (log.isDebugEnabled()) {
            log.debug("收到 LiveKit 事件, 数据内容为: {}", JsonFormat.printer().print(event));
        }

        if (!"participant_left".equals(event.getEvent())) {
            return ;
        }

        switch (event.getParticipant().getDisconnectReason()) {
            case CLIENT_INITIATED, UNKNOWN_REASON -> delayCompleted(event);
        }
    }

    static public Long getUserChatCallId(String roomName) {
        return NumberUtils.toLong(StringUtils.substringAfter(roomName, UserChatCallEntity.ROOM_ID_PREFIX));
    }

    public RMapCache<Long, String> getMapCache() {
        return redissonClient.getMapCache(DELAY_MAP_CACHE_KEY);
    }
}
