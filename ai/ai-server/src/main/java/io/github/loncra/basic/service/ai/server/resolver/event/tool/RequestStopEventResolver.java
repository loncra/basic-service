package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.RequestStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestStopEventResolver extends AbstractAgentEventResolver<CustomizeMetadata> {

    public static final String USER_ID_KEY = "userId";
    public static final String SESSION_ID_KEY = "sessionId";
    public static final String STOP_REASON_KEY = "stopReason";

    @Override
    protected List<CustomizeMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        RequestStopEvent requestStopEvent = CastUtils.cast(event);
        CustomizeMetadata metadata = new CustomizeMetadata();
        metadata.setEventType(AgentMessageContentTypeEnum.REQUEST_STOP);
        metadata.getMetadata().put(STOP_REASON_KEY, requestStopEvent.getGenerateReason());
        metadata.getMetadata().put(USER_ID_KEY, context.getUserId());
        metadata.getMetadata().put(SESSION_ID_KEY, context.getSessionId());
        return List.of(metadata);
    }

    @Override
    public boolean postPublish(
            CustomizeMetadata content,
            AgentMessageEntity assistant
    ) {
        assistant.setStatus(AgentChatStatusEnum.REQUEST_STOP);
        assistant.getMetadata().put(content.getEventSource().getType().toString(), content.getMetadata().get(STOP_REASON_KEY));
        getAgentMessageService().updateById(assistant);
        return true;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return RequestStopEvent.class.isAssignableFrom(event.getClass());
    }
}
