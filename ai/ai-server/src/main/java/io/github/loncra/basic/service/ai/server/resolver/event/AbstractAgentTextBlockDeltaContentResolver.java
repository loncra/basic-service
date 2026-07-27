package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Objects;

public abstract class AbstractAgentTextBlockDeltaContentResolver extends AbstractAgentEventResolver<AgentTextContentMetadata> {

    @Override
    protected AgentTextContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        String delta = getDelta(event);
        AgentTextContentMetadata content = new AgentTextContentMetadata();
        content.setEventType(getTextType());
        content.setId(getReplyId(event));
        content.setValue(delta);
        content.setStatus(AgentBlockStatusEnum.RUNNING);
        content.setCreationTime(Instant.now());
        return content;
    }

    protected abstract String getDelta(AgentEvent event);

    protected abstract AgentMessageContentTypeEnum getTextType();

    @Override
    public boolean postPublish(
            AgentTextContentMetadata content,
            AgentMessageEntity assistant
    ) {
        String id = getReplyId(content.getEventSource());
        AgentTextContentMetadata metadata = assistant.obtainBlock(id, AgentTextContentMetadata.class);
        if (Objects.isNull(metadata)) {
            metadata = CastUtils.of(content, AgentTextContentMetadata.class);
            metadata.setCreationTime(Instant.now());
        } else {
            metadata.setValue(metadata.getValue() + StringUtils.defaultIfEmpty(content.getValue(), StringUtils.EMPTY));
        }
        assistant.updateContent(metadata);

        return false;
    }

    protected abstract String getReplyId(AgentEvent event);

}
