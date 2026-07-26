package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockEndEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class TextBlockEndEventResolver extends AbstractAgentTextEndContentResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return TextBlockEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected AgentMessageContentTypeEnum getTextType() {
        return AgentMessageContentTypeEnum.ANSWER;
    }

    @Override
    protected String getReplyId(AgentEvent event) {
        TextBlockEndEvent endEvent = CastUtils.cast(event);
        return AgentMessageContentTypeEnum.ANSWER.getValue() + CacheProperties.DEFAULT_SEPARATOR + endEvent.getReplyId();
    }
}
