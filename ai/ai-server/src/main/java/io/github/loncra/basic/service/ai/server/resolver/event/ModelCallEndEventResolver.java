package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModelCallEndEventResolver implements AgentEventResolver {

    private final List<AgentChannelResolver> channelResolvers;

    private AgentChannelResolver channelResolver(ExecutionChannelEnum channel) {
        return AgentChannelResolver.channelResolver(channelResolvers, channel);
    }

    @Override
    public boolean isSupport(AgentEvent event) {
        return ModelCallEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public List<AbstractAssistantMessageContentMetadata> process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        ModelCallEndEvent modelCallEndEvent = CastUtils.cast(event);

        AgentTokenUsageMetadata usage = CastUtils.of(modelCallEndEvent.getUsage(), AgentTokenUsageMetadata.class);
        usage.setUsageType(AgentMessageContentTypeEnum.MODEL_CALL_END);
        usage.setId(modelCallEndEvent.getId());

        List<AbstractAssistantMessageContentMetadata> exist = assistant.obtainBlock(modelCallEndEvent.getReplyId());
        if (exist.size() == BigDecimal.ONE.intValue()) {
            AgentMessageContentTypeEnum usageType = ValueEnum.ofEnum(AgentMessageContentTypeEnum.class, exist.getLast().getType());
            usage.setUsageType(usageType);
        }
        assistant.saveAgentTokenUsageMetadata(usage);
        AgentExecutionMetadata execution = context.get(AgentExecutionMetadata.class);
        channelResolver(execution.getChannel()).getAgentRecordStore().saveAssistant(execution, assistant);

        usage.setAssistantMessageId(assistant.getId());

        return List.of(usage);
    }
}
