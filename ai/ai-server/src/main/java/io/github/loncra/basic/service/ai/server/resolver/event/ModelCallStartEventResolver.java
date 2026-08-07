package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallStartEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.number.NumberIdEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModelCallStartEventResolver implements AgentEventResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return ModelCallStartEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public List<AbstractAssistantMessageContentMetadata> process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        ModelCallStartEvent modelCallStartEvent = CastUtils.cast(event);
        Instant creationTime = Instant.parse(modelCallStartEvent.getCreatedAt());
        assistant.setModelCallStartTimeMetadata(creationTime);

        CustomizeMetadata metadata = new CustomizeMetadata();
        metadata.setEventType(AgentMessageContentTypeEnum.MODEL_CALL_START);
        metadata.setAssistantMessageId(assistant.getId());
        metadata.setId(modelCallStartEvent.getId());
        metadata.getMetadata().put(NumberIdEntity.CREATION_TIME_FIELD_NAME, creationTime);

        return List.of(metadata);
    }
}
