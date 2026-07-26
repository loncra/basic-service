package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.PersistenceAgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.util.Objects;

public abstract class AbstractAgentTextEndContentResolver extends AbstractAgentEventResolver<AgentTextContentMetadata> {

    @Override
    public boolean postPublish(
            AgentTextContentMetadata content,
            AgentMessageEntity assistant
    ) {
        String id = getReplyId(content.getEventSource());
        PersistenceAgentTextContentMetadata metadata = assistant.obtainBlock(id, PersistenceAgentTextContentMetadata.class);
        if (Objects.isNull(metadata)) {
            return false;
        }
        BeanUtils.copyProperties(content, metadata);
        assistant.updateContent(metadata);
        updateAssistantContent(assistant);
        return true;
    }

    @Override
    protected AgentTextContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        PersistenceAgentTextContentMetadata metadata = new PersistenceAgentTextContentMetadata();
        metadata.setEventType(getTextType());
        metadata.setId(getReplyId(event));
        metadata.setEndTime(Instant.now());
        return metadata;
    }

    protected abstract AgentMessageContentTypeEnum getTextType();

    protected abstract String getReplyId(AgentEvent event);

}
