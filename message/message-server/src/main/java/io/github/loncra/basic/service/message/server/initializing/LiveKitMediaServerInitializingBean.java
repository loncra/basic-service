package io.github.loncra.basic.service.message.server.initializing;

import io.github.loncra.basic.service.message.server.event.LiveKitMediaServerWebhookEventListener;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMapCache;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveKitMediaServerInitializingBean implements InitializingBean {

    private final LiveKitMediaServerWebhookEventListener liveKitMediaServerWebhookEventListener;

    @Override
    public void afterPropertiesSet() {
        RMapCache<Long, String> mapCache = liveKitMediaServerWebhookEventListener.getMapCache();
        mapCache.addListener((EntryExpiredListener<Long, String>) this::onRedissonExpiredListener);
    }

    public void onRedissonExpiredListener(EntryEvent<Long, String> event) {
        Long key = event.getKey();
        String value = event.getValue();
        liveKitMediaServerWebhookEventListener.completedChatCall(key, value);
    }
}
