package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThinkingBlockDeltaEventResolver extends AbstractAgentTextBlockDeltaContentResolver {


    @Override
    public boolean isSupport(AgentEvent event) {
        return ThinkingBlockDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected String getDelta(AgentEvent event) {
        return CastUtils.cast(event, ThinkingBlockDeltaEvent.class).getDelta();
    }

    @Override
    protected AgentMessageContentTypeEnum getTextType() {
        return AgentMessageContentTypeEnum.THINK;
    }

    @Override
    protected String getReplyId(AgentEvent event) {
        ThinkingBlockDeltaEvent delta = CastUtils.cast(event);
        return AgentMessageContentTypeEnum.THINK.getValue() + CacheProperties.DEFAULT_SEPARATOR + delta.getReplyId();
    }
}
