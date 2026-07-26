package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ThinkingBlockEndEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class ThinkBlockEndEventResolver extends AbstractAgentTextEndContentResolver {

    @Override
    protected AgentMessageContentTypeEnum getTextType() {
        return AgentMessageContentTypeEnum.THINK;
    }

    @Override
    protected String getReplyId(AgentEvent event) {
        ThinkingBlockEndEvent endEvent = CastUtils.cast(event);
        return AgentMessageContentTypeEnum.THINK.getValue() + CacheProperties.DEFAULT_SEPARATOR + endEvent.getReplyId();
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ThinkingBlockEndEvent.class.isAssignableFrom(event.getClass());
    }
}
