package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockDeltaContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;

public abstract class AbstractBlockDeltaContentResolver extends AbstractAgentEventResolver<AbstractBlockDeltaContentMetadata> {

    @Override
    protected List<AbstractBlockDeltaContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {

        SystemException.isTrue(AbstractBlockDeltaContentMetadata.class.isAssignableFrom(getDeltaType().getTargetClass()), getDeltaType().getTargetClass() + "未实现 BlockRunningContentMetadata 元数据");

        String delta = getDelta(event);

        AbstractAssistantMessageContentMetadata metadata = SystemException.convertSupplier(() -> BeanUtils.instantiateClass(getDeltaType().getTargetClass()));

        AbstractBlockDeltaContentMetadata content = CastUtils.cast(metadata);
        content.setId(getBlockId(event));
        content.setValue(delta);
        return List.of(content);
    }

    protected abstract String getDelta(AgentEvent event);

    @Override
    public boolean postPublish(
            AbstractBlockDeltaContentMetadata content,
            AgentMessageEntity assistant
    ) {
        String id = getBlockId(content.getEventSource());
        AbstractBlockDeltaContentMetadata metadata = assistant.obtainBlock(id, getDeltaType());
        if (Objects.isNull(metadata)) {
            return false;
        }
        String current = StringUtils.defaultIfEmpty(metadata.getValue(), StringUtils.EMPTY);
        String delta = StringUtils.defaultIfEmpty(content.getValue(), StringUtils.EMPTY);

        metadata.setValue(current + delta);
        assistant.updateContent(metadata);

        return false;
    }

    protected abstract String getBlockId(AgentEvent event);

    protected abstract AgentMessageContentTypeEnum getDeltaType();
}
