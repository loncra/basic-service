package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@Setter(onMethod_ = @Autowired)
public abstract class AbstractAgentEventResolver<T extends AgentAssistantMessageContent> implements AgentEventResolver {

    //private AgentSseStreamPublishResolver agentSseStreamPublishResolver;

    @Getter
    private AgentMessageService agentMessageService;

    @Override
    public T process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        T content = createPublishPatchContent(event, context);
        content.setEventSource(event);
        return content;
    }

    public boolean postPublish(
            T content,
            AgentMessageEntity assistant
    ) {
        return false;
    }

    protected abstract T createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    );

    protected void updateAssistantContent(AgentMessageEntity assistant) {
        agentMessageService.lambdaUpdate()
                .set(AgentChatMetadata::getContent, assistant.obtainContentJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();
    }
}
