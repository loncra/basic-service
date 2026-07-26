package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.PersistenceAgentTextContentMetadata;
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
        return AgentTextContentMetadata.of(
                getTextType(),
                getReplyId(event),
                delta
        );
    }

    protected abstract String getDelta(AgentEvent event);

    protected abstract AgentMessageContentTypeEnum getTextType();

    @Override
    public boolean postPublish(
            AgentTextContentMetadata content,
            AgentMessageEntity assistant
    ) {
        String id = getReplyId(content.getEventSource());
        PersistenceAgentTextContentMetadata metadata = assistant.obtainBlock(id, PersistenceAgentTextContentMetadata.class);
        if (Objects.isNull(metadata)) {
            metadata = CastUtils.of(content, PersistenceAgentTextContentMetadata.class);
            metadata.setCreationTime(Instant.now());
        }
        metadata.setValue(metadata.getValue() + StringUtils.defaultIfEmpty(content.getValue(), StringUtils.EMPTY));
        assistant.updateContent(metadata);

        return false;
    }

    protected abstract String getReplyId(AgentEvent event);

}
