package io.github.loncra.basic.service.ai.server.service.agent;

import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AgentExecutionStore {

    public static final String KEY_PREFIX = "loncra:basic-service:ai:app:agent:execute:";

    private static final Duration TTL = Duration.ofHours(2);

    private final RedissonClient redissonClient;

    public String create(AgentExecutionMetadata execution) {
        String executionId = UUID.randomUUID().toString();
        bucket(executionId).set(execution, TTL);
        return executionId;
    }

    public AgentExecutionMetadata get(String executionId) {
        return bucket(executionId).get();
    }

    public void update(String executionId, AgentExecutionMetadata execution) {
        bucket(executionId).set(execution, TTL);
    }

    public void remove(String executionId) {
        bucket(executionId).delete();
    }

    private RBucket<AgentExecutionMetadata> bucket(String executionId) {
        return redissonClient.getBucket(KEY_PREFIX + executionId);
    }
}
