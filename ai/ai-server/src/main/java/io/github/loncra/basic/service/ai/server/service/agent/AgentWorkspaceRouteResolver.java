package io.github.loncra.basic.service.ai.server.service.agent;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentWorkspaceRouteResolver {

    public static final String ROUTE_KEY_PREFIX = "loncra:basic-service:ai:app:agent:workspace:route:";

    private static final Duration ROUTE_TTL = Duration.ofDays(7);

    private final RedissonClient redissonClient;

    public void bind(Long workspaceId, String deviceIdentified) {
        if (Objects.isNull(workspaceId) || StringUtils.isBlank(deviceIdentified)) {
            return;
        }
        RBucket<String> bucket = redissonClient.getBucket(ROUTE_KEY_PREFIX + workspaceId, StringCodec.INSTANCE);
        bucket.set(deviceIdentified, ROUTE_TTL);
    }

    public Optional<String> resolve(Long workspaceId) {
        if (Objects.isNull(workspaceId)) {
            return Optional.empty();
        }
        RBucket<String> bucket = redissonClient.getBucket(ROUTE_KEY_PREFIX + workspaceId, StringCodec.INSTANCE);
        return Optional.ofNullable(bucket.get());
    }
}
