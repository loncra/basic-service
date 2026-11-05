package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEndEvent;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusContentMetadata;
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

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentEndEventResolver implements AgentEventResolver {

    private final List<AgentChannelResolver> channelResolvers;

    private AgentChannelResolver channelResolver(ExecutionChannelEnum channel) {
        return AgentChannelResolver.channelResolver(channelResolvers, channel);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return AgentEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public List<AbstractAssistantMessageContentMetadata> process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        List<AbstractAssistantMessageContentMetadata> result = new LinkedList<>();
        AgentExecutionMetadata execution = context.get(AgentExecutionMetadata.class);
        if (assistant.getStatus() == AgentChatStatusEnum.RUNNING) {
            assistant.setStatus(AgentChatStatusEnum.COMPLETED);
            channelResolver(execution.getChannel()).saveAssistant(execution, assistant);
            AgentEndEvent endEvent = CastUtils.cast(event);

            CustomizeMetadata metadata = new CustomizeMetadata();
            metadata.setId(endEvent.getReplyId());
            metadata.setEventType(AgentMessageContentTypeEnum.STREAM_STOP);
            metadata.setAssistantMessageId(assistant.getId());
            metadata.getMetadata().put(SystemConstants.STATUS_TABLE_FIELD_NAME, assistant.getStatus());
            result.add(metadata);
        }
        channelResolver(execution.getChannel()).saveConversationStatus(execution, assistant.getStatus());

        AgentStatusContentMetadata metadata = new AgentStatusContentMetadata();
        metadata.setId(assistant.getAgentConversationId().toString());
        metadata.setStatus(assistant.getStatus());
        metadata.setAssistantMessageId(assistant.getId());

        result.add(metadata);

        return result;
    }
}
