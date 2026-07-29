package io.github.loncra.basic.service.ai.server.resolver.event.tool;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ThinkBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.ToolCallBlockContentMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentMessageContentTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.event.AbstractAgentEventResolver;
import io.github.loncra.framework.commons.CastUtils;

import java.util.Optional;

public abstract class UpdateToolCallDataResolver extends AbstractAgentEventResolver<ToolCallBlockContentMetadata> {

    @Override
    public boolean postPublish(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    ) {

        Optional<ThinkBlockContentMetadata> optional = assistant.obtainMessageContents()
                .stream()
                .filter(s -> AgentMessageContentTypeEnum.THINK.getValue().equals(s.getType()))
                .map(s -> CastUtils.cast(s, ThinkBlockContentMetadata.class))
                .filter(s -> s.getToolCall().getId().equals(content.getId()))
                .findFirst();

        CopyOptions options = CopyOptions.create().ignoreNullValue();
        if (optional.isEmpty()) {
            ToolCallBlockContentMetadata exist = assistant.obtainBlock(content.getId(), AgentMessageContentTypeEnum.TOOL_CALL);
            BeanUtil.copyProperties(content, exist, options);
            assistant.updateContent(exist);
        } else {
            ThinkBlockContentMetadata thinking = optional.get();
            BeanUtil.copyProperties(content, thinking.getToolCall(), options);
            assistant.updateContent(thinking);
        }

        return postUpdate(content, assistant);
    }

    protected abstract boolean postUpdate(
            ToolCallBlockContentMetadata content,
            AgentMessageEntity assistant
    );
}
