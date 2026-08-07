package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

@Setter(onMethod_ = @Autowired)
public abstract class AbstractAgentEventResolver<T extends AbstractAssistantMessageContentMetadata> implements AgentEventResolver {

    @Getter
    private AgentMessageService agentMessageService;

    @Override
    public List<AbstractAssistantMessageContentMetadata> process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        List<T> contents = createPublishPatchContent(event, context);
        for (T content : contents) {
            content.setEventSource(event);
            content.setAssistantMessageId(assistant.getId());
        }
        return new LinkedList<>(contents);
    }

    public boolean postPublish(
            T content,
            AgentMessageEntity assistant
    ) {
        return false;
    }

    protected abstract List<T> createPublishPatchContent(
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
