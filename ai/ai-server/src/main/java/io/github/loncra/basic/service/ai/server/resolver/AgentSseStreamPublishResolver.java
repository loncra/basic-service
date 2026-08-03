package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AgentSseStreamPublishResolver {

    void publish(
            String conversationId,
            AbstractAssistantMessageContentMetadata content
    );

    void clean(
            String conversationId,
            String streamId
    );

    Flux<ServerSentEvent<String>> open(AgentMessageEntity assistant);

    default List<ServerSentEvent<String>> getAgentMessageServerSentEvent(AgentMessageEntity assistant){
        return assistant.obtainMessageContents()
                .stream()
                .map(AbstractAssistantMessageContentMetadata::toServerSentEvent)
                .toList();
    }

    void remove(String conversationId);

    boolean isCompleted(String conversationId);
}
