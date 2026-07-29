package io.github.loncra.basic.service.ai.server.resolver.event.think;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractBlockDeltaContentResolver;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThinkBlockDeltaEventResolver extends AbstractBlockDeltaContentResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return ThinkingBlockDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected String getDelta(AgentEvent event) {
        return CastUtils.cast(event, ThinkingBlockDeltaEvent.class).getDelta();
    }

    @Override
    protected String getBlockId(AgentEvent event) {
        ThinkingBlockDeltaEvent delta = CastUtils.cast(event);
        return delta.getReplyId();
    }

    @Override
    protected AgentMessageContentTypeEnum getDeltaType() {
        return AgentMessageContentTypeEnum.THINK;
    }
}
