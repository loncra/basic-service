package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ModelCompletedEventResolver implements AgentEventResolver {

    private final AgentMessageService agentMessageService;

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
        usage.setId(assistant.getId().toString());

        List<AbstractAssistantMessageContentMetadata> exist = assistant.obtainBlock(modelCallEndEvent.getReplyId());
        if (exist.size() == BigDecimal.ONE.intValue()) {
            AgentMessageContentTypeEnum usageType = ValueEnum.ofEnum(AgentMessageContentTypeEnum.class, exist.getLast().getType());
            usage.setUsageType(usageType);
        }
        assistant.saveAgentTokenUsageMetadata(usage);
        if (AgentChatStatusEnum.RUNNING.equals(assistant.getStatus())) {
            assistant.setStatus(AgentChatStatusEnum.COMPLETED);
            agentMessageService.lambdaUpdate()
                    .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.COMPLETED.getValue())
                    .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                    .eq(AgentMessageEntity::getId, assistant.getId())
                    .update();
        }

        return List.of(usage);
    }
}
