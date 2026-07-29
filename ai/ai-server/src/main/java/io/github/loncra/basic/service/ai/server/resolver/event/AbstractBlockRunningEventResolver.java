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

public abstract class AbstractBlockRunningEventResolver extends AbstractAgentEventResolver<AbstractBlockRunningContentMetadata> {

    @Override
    protected AbstractBlockRunningContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        SystemException.isTrue(AbstractBlockRunningContentMetadata.class.isAssignableFrom(getRunningType().getTargetClass()), getRunningType().getTargetClass() + "未实现 BlockRunningContentMetadata 元数据");

        AbstractAssistantMessageContentMetadata metadata = SystemException.convertSupplier(() -> BeanUtils.instantiateClass(getRunningType().getTargetClass()));
        AbstractBlockRunningContentMetadata runningContentMetadata = CastUtils.cast(metadata);
        runningContentMetadata.setCreationTime(Instant.now());
        runningContentMetadata.setId(getBlockId(event));
        runningContentMetadata.setStatus(AgentBlockStatusEnum.RUNNING);

        return runningContentMetadata;
    }

    @Override
    public boolean postPublish(
            AbstractBlockRunningContentMetadata content,
            AgentMessageEntity assistant
    ) {
        assistant.updateContent(content);
        return false;
    }

    protected abstract String getBlockId(AgentEvent event);

    protected abstract AgentMessageContentTypeEnum getRunningType();
}
