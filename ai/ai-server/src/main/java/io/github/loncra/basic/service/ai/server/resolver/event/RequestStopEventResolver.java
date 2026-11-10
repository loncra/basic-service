package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.RequestStopEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.CustomizeMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
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
            AgentMessageEntity assistant,
            RuntimeContext context
    ) {
        assistant.setStatus(AgentChatStatusEnum.REQUEST_STOP);
        AgentExecutionMetadata execution = Objects.requireNonNull(context.get(AgentExecutionMetadata.class), "找不到执行元数据");
        channelResolver(execution.getChannel()).getAgentRecordStore().saveAssistant(execution, assistant);
        return true;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return RequestStopEvent.class.isAssignableFrom(event.getClass());
    }
}
