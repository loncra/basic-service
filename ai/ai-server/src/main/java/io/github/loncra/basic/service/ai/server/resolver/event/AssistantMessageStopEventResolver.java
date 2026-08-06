package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.AssistantMessageStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.id.IdEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AssistantMessageStopEventResolver implements AgentEventResolver {

    private final AgentMessageService agentMessageService;

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

        assistant.setStatus(AgentChatStatusEnum.STOPPED);
        agentMessageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, assistant.getStatus().getValue())
                .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                .set(AgentChatMetadata::getContent, assistant.obtainContentJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();

        CustomizeMetadata metadata = new CustomizeMetadata();
        metadata.setId(assistant.getId().toString());
        metadata.setEventType(AgentMessageContentTypeEnum.STREAM_STOP);

        return List.of(metadata);
    }
}
