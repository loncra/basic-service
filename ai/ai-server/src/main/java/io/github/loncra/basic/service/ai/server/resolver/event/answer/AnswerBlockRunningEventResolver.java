package io.github.loncra.basic.service.ai.server.resolver.event.answer;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockStartEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractBlockRunningEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class AnswerBlockRunningEventResolver extends AbstractBlockRunningEventResolver {

    @Override
    protected String getBlockId(AgentEvent event) {
        TextBlockStartEvent start = CastUtils.cast(event);
        return start.getReplyId();
    }

    @Override
    protected AgentMessageContentTypeEnum getRunningType() {
        return AgentMessageContentTypeEnum.ANSWER;
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return TextBlockStartEvent.class.isAssignableFrom(event.getClass());
    }
}
