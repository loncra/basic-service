package io.github.loncra.basic.service.ai.server.resolver;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

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

    Flux<ServerSentEvent<String>> open(AgentMessageEntity assistant,boolean loadHistory);

    default List<ServerSentEvent<String>> getAgentMessageServerSentEvent(AgentMessageEntity assistant){
        return assistant.obtainMessageContents()
                .stream()
                .map(AbstractAssistantMessageContentMetadata::toServerSentEvent)
                .toList();
    }

    void remove(String conversationId);

    boolean isCompleted(String conversationId);

    void interrupt(AgentMessageEntity assistant);

    Object listenerInterrupt(
            Agent agent,
            RuntimeContext ctx,
            Sinks.Many<AgentEvent> stopEventSink
    );

    void cleanListener(
            SignalType signalType,
            RuntimeContext ctx,
            Object returnValue
    );

    boolean isStreamBeforeInterrupt(RuntimeContext ctx);
}
