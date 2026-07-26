package io.github.loncra.basic.service.ai.server.enumerate.agent;


import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentStatusChangeContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTextContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentTokenUsageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.AgentToolCallContentMetadata;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentMessageContentTypeEnum implements NameValueEnum<String> {

    THINK("思考", "think", AgentTextContentMetadata.class),

    TOOL_START("执行工具调用", "toolStart", AgentToolCallContentMetadata.class),

    TOOL_END("工具调用完成", "toolEnd", AgentToolCallContentMetadata.class),

    ANSWER("回答", "answer", AgentTextContentMetadata.class),

    ERROR("错误", "error", AgentTextContentMetadata.class),

    AGENT_STATUS_CHANGE("智能体状态变更", "agentStatusChange", AgentStatusChangeContentMetadata.class),

    MODEL_COMPLETED("模型执行完成", "modelCompleted", AgentTokenUsageContentMetadata.class),

    COMPLETED("完成","completed", AgentTextContentMetadata.class),

    ASSISTANT("助手消息", "assistant", AgentAssistantMessageContent.class),

    GENERATE_CONVERSATION_NAME("生成会话名称", "generateConversationName", AgentTextContentMetadata.class)
    ;

    private final String name;

    private final String value;

    private final Class<? extends AgentAssistantMessageContent> targetClass;

    public static final List<String> COMPLETED_STATUS = List.of(ERROR.getValue(), COMPLETED.getValue());

    public static final List<AgentMessageContentTypeEnum> TEXT_BLOCK_TYPE = List.of(THINK, ANSWER, ERROR, COMPLETED, GENERATE_CONVERSATION_NAME);
}
