package io.github.loncra.basic.service.ai.server.resolver.event.answer;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockEndEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentTextEndContentResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class AnswerBlockEndEventResolver extends AbstractAgentTextEndContentResolver {

    @Override
    protected String getBlockId(AgentEvent event) {
        TextBlockEndEvent end = CastUtils.cast(event);
        return end.getReplyId();
    }

    @Override
    protected AgentMessageContentTypeEnum getEndType() {
        return AgentMessageContentTypeEnum.ANSWER;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return TextBlockEndEvent.class.isAssignableFrom(event.getClass());
    }
}
