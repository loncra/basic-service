package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TextBlockDeltaEventResolver extends AbstractAgentTextBlockDeltaContentResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return TextBlockDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected String getDelta(AgentEvent event) {
        return CastUtils.cast(event, TextBlockDeltaEvent.class).getDelta();
    }

    @Override
    protected AgentMessageContentTypeEnum getTextType() {
        return AgentMessageContentTypeEnum.ANSWER;
    }

    @Override
    protected String getReplyId(AgentEvent event) {
        TextBlockDeltaEvent delta = CastUtils.cast(event);
        return AgentMessageContentTypeEnum.ANSWER.getValue() + CacheProperties.DEFAULT_SEPARATOR + delta.getReplyId();
    }
}
