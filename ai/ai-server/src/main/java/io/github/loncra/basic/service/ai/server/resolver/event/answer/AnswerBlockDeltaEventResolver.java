package io.github.loncra.basic.service.ai.server.resolver.event.answer;

import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractBlockDeltaContentResolver;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerBlockDeltaEventResolver extends AbstractBlockDeltaContentResolver {

    @Override
    public boolean isSupport(AgentEvent event) {
        return TextBlockDeltaEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    protected String getDelta(AgentEvent event) {
        return CastUtils.cast(event, TextBlockDeltaEvent.class).getDelta();
    }

    @Override
    protected String getBlockId(AgentEvent event) {
        TextBlockDeltaEvent delta = CastUtils.cast(event);
        return delta.getReplyId();
    }

    @Override
    protected AgentMessageContentTypeEnum getDeltaType() {
        return AgentMessageContentTypeEnum.ANSWER;
    }
}
