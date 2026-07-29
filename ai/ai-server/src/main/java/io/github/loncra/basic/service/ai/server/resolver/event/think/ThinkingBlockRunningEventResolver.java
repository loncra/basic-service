package io.github.loncra.basic.service.ai.server.resolver.event.think;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ThinkingBlockStartEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractBlockRunningEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class ThinkingBlockRunningEventResolver extends AbstractBlockRunningEventResolver {

    @Override
    protected String getBlockId(AgentEvent event) {
        ThinkingBlockStartEvent start = CastUtils.cast(event);
        return start.getReplyId();
    }

    @Override
    protected AgentMessageContentTypeEnum getRunningType() {
        return AgentMessageContentTypeEnum.THINK;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ThinkingBlockStartEvent.class.isAssignableFrom(event.getClass());
    }
}
