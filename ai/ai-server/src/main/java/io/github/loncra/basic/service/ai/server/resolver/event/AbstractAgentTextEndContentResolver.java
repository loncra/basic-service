package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.ai.api.enumerate.AgentBlockStatusEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractBlockRunningContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public abstract class AbstractAgentTextEndContentResolver extends AbstractAgentEventResolver<AbstractBlockRunningContentMetadata> {

    @Override
    public boolean postPublish(
            AbstractBlockRunningContentMetadata content,
            AgentMessageEntity assistant
    ) {
        String id = getBlockId(content.getEventSource());
        AbstractBlockRunningContentMetadata metadata = assistant.obtainBlock(id, getEndType());
        if (Objects.isNull(metadata)) {
            return false;
        }

        metadata.setEndTime(content.getEndTime());
        metadata.setStatus(content.getStatus());

        assistant.updateContent(metadata);
        updateAssistantContent(assistant);
        return true;
    }

    @Override
    protected List<AbstractBlockRunningContentMetadata> createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {

        SystemException.isTrue(AbstractBlockRunningContentMetadata.class.isAssignableFrom(getEndType().getTargetClass()), getEndType().getTargetClass() + "未实现 BlockRunningContentMetadata 元数据");

        AbstractAssistantMessageContentMetadata metadata = SystemException.convertSupplier(() -> BeanUtils.instantiateClass(getEndType().getTargetClass()));

        AbstractBlockRunningContentMetadata runningContent = CastUtils.cast(metadata);
        runningContent.setId(getBlockId(event));
        runningContent.setStatus(AgentBlockStatusEnum.DONE);
        runningContent.setEndTime(Instant.now());

        return List.of(runningContent);
    }

    protected abstract String getBlockId(AgentEvent event);

    protected abstract AgentMessageContentTypeEnum getEndType();
}
