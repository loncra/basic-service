package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.AssistantMessageStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssistantMessageStopEventResolver implements AgentEventResolver {

    private final List<AgentChannelResolver> channelResolvers;

    private AgentChannelResolver channelResolver(ExecutionChannelEnum channel) {
        return AgentChannelResolver.channelResolver(channelResolvers, channel);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return AssistantMessageStopEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public List<AbstractAssistantMessageContentMetadata> process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        AssistantMessageStopEvent stopEvent = CastUtils.cast(event);
        assistant.setStatus(AgentChatStatusEnum.STOPPED);
        AgentExecutionMetadata execution = context.get(AgentExecutionMetadata.class);
        channelResolver(execution.getChannel()).getAgentRecordStore().saveAssistant(execution, assistant);

        CustomizeMetadata metadata = new CustomizeMetadata();
        metadata.setId(stopEvent.getId());
        metadata.setEventType(AgentMessageContentTypeEnum.STREAM_STOP);
        metadata.setAssistantMessageId(assistant.getId());
        metadata.getMetadata().put(SystemConstants.STATUS_TABLE_FIELD_NAME, assistant.getStatus());

        return List.of(metadata);
    }
}
