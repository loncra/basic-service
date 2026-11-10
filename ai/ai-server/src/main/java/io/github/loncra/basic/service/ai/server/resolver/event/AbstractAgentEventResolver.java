package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Setter(onMethod_ = @Autowired)
public abstract class AbstractAgentEventResolver<T extends AbstractAssistantMessageContentMetadata> implements AgentEventResolver {

    @Getter
    private List<AgentChannelResolver> channelResolvers;

    protected AgentChannelResolver channelResolver(ExecutionChannelEnum channel) {
        return AgentChannelResolver.channelResolver(channelResolvers, channel);
    }

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
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        return false;
    }

    protected abstract List<T> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    );

    protected void updateAssistantContent(AgentMessageEntity assistant, RuntimeContext context) {
        AgentExecutionMetadata execution = Objects.requireNonNull(
                Objects.isNull(context) ? null : context.get(AgentExecutionMetadata.class),
                "找不到执行元数据，无法持久化助手消息"
        );
        channelResolver(execution.getChannel()).getAgentRecordStore().saveAssistant(execution, assistant);
    }

}
