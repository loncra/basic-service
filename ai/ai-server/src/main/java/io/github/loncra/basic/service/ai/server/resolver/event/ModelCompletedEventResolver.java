package io.github.loncra.basic.service.ai.server.resolver.event;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.AgentChatMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.AgentEventResolver;
import io.github.loncra.basic.service.ai.server.service.agent.AgentMessageService;
import io.github.loncra.framework.commons.CastUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelCompletedEventResolver implements AgentEventResolver {

    private final AgentMessageService agentMessageService;

    /*@Override
    protected AgentTokenUsageContentMetadata createPublishPatchContent(
            AgentEvent event,
            RuntimeContext context
    ) {
        ModelCallEndEvent modelCallEndEvent = CastUtils.cast(event);

        AgentTokenUsageContentMetadata usage = CastUtils.of(modelCallEndEvent.getUsage(), AgentTokenUsageContentMetadata.class);
        usage.setTokenType(AgentMessageContentTypeEnum.MODEL_COMPLETED);
        usage.setId();
        return usage;
    }*/

    /*@Override
    public boolean postPublish(
            AgentTokenUsageContentMetadata content,
            AgentMessageEntity assistant
    ) {
        assistant.saveAgentTokenUsageMetadata(content);
        assistant.setStatus(AgentChatStatusEnum.COMPLETED);
        getAgentMessageService().lambdaUpdate()
                .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.COMPLETED.getValue())
                .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();
        return true;
    }*/

    @Override
    public boolean isSupport(AgentEvent event) {
        return ModelCallEndEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public AgentAssistantMessageContent process(
            AgentMessageEntity assistant,
            AgentEvent event,
            RuntimeContext context
    ) {
        ModelCallEndEvent modelCallEndEvent = CastUtils.cast(event);

        AgentTokenUsageContentMetadata usage = CastUtils.of(modelCallEndEvent.getUsage(), AgentTokenUsageContentMetadata.class);
        usage.setTokenType(AgentMessageContentTypeEnum.MODEL_COMPLETED);
        usage.setId(assistant.getId().toString());

        assistant.saveAgentTokenUsageMetadata(usage);
        assistant.setStatus(AgentChatStatusEnum.COMPLETED);
        agentMessageService.lambdaUpdate()
                .set(AgentMessageEntity::getStatus, AgentChatStatusEnum.COMPLETED.getValue())
                .set(AgentChatMetadata::getMetadata, assistant.obtainMetadataJsonString())
                .eq(AgentMessageEntity::getId, assistant.getId())
                .update();

        return usage;
    }
}
