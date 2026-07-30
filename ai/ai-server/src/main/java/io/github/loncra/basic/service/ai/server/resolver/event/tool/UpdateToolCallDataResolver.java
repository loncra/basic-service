package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;

public abstract class UpdateToolCallDataResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {

        ToolCallBlockContentMetadata exist = assistant.obtainBlock(content.getId(), AgentMessageContentTypeEnum.TOOL_CALL);
        BeanUtil.copyProperties(content, exist, CopyOptions.create().ignoreNullValue());
        assistant.updateContent(exist);
        return postUpdate(content, assistant);
    }

    protected abstract boolean postUpdate(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    );
}
