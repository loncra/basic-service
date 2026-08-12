package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ConfirmResult;
import io.agentscope.core.event.UserConfirmResultEvent;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

@Component
public class UserConfirmResultEventResolver extends UpdateToolCallDataResolver {

    @Override
    protected List<ToolCallBlockContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        UserConfirmResultEvent confirmEvent = CastUtils.cast(event);
        List<ToolCallBlockContentMetadata> result = new LinkedList<>();
        for (ConfirmResult confirmResult : confirmEvent.getConfirmResults()) {
            ToolCallBlockContentMetadata metadata = new ToolCallBlockContentMetadata();
            metadata.setId(confirmResult.getToolCall().getId());
            metadata.setHitlStatus(confirmResult.getToolCall().getState());
            metadata.setStatus(AgentBlockStatusEnum.DONE);
            metadata.setUserConfirmed(confirmResult.isConfirmed());
            metadata.setEndTime(Instant.now());
            result.add(metadata);
        }
        return result;
    }

    @Override
    protected boolean postUpdate(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {
        updateAssistantContent(assistant);
        return true;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return UserConfirmResultEvent.class.isAssignableFrom(event.getClass());
    }
}
