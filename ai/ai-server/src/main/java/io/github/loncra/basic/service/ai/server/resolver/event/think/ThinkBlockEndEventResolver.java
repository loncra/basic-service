package io.github.loncra.basic.service.ai.server.resolver.event.think;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ThinkingBlockEndEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentTextEndContentResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class ThinkBlockEndEventResolver extends AbstractAgentTextEndContentResolver {

    @Override
    protected String getBlockId(AgentEvent event) {
        ThinkingBlockEndEvent end = CastUtils.cast(event);
        return end.getReplyId();
    }

    @Override
    protected AgentMessageContentTypeEnum getEndType() {
        return AgentMessageContentTypeEnum.THINK;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ThinkingBlockEndEvent.class.isAssignableFrom(event.getClass());
    }
}
