package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AgentSseStreamPublishResolver {

    void publish(
            String conversationId,
            AgentAssistantMessageContent content
    );

    void clean(
            String conversationId,
            String streamId
    );

    Flux<ServerSentEvent<String>> open(AgentMessageEntity assistant);

    default List<ServerSentEvent<String>> getAgentMessageServerSentEvent(AgentMessageEntity assistant){
        return assistant.obtainMessageContents()
                .stream()
                .map(AgentAssistantMessageContent::toServerSentEvent)
                .toList();
    }

    void remove(String string);

    boolean isCompleted(String conversationId);
}
