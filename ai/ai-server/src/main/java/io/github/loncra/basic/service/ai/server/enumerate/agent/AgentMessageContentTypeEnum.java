package io.github.loncra.basic.service.ai.server.enumerate.agent;


import io.github.loncra.basic.service.ai.server.domain.metadata.AgentAssistantMessageContent;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.*;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentMessageContentTypeEnum implements NameValueEnum<String> {

    THINK("思考", "think", AgentTextContentMetadata.class),

    TOOL("执行工具", "tool", AgentToolCallContentMetadata.class),

    ANSWER("回答", "answer", AgentTextContentMetadata.class),

    ERROR("错误", "error", CustomizeContentMetadata.class),

    AGENT_STATUS_CHANGE("智能体状态变更", "agentStatusChange", AgentStatusChangeContentMetadata.class),

    STREAM_START("流开始", "streamStart", CustomizeContentMetadata.class),

    STREAM_END("流结束", "streamEnd", CustomizeContentMetadata.class),

    TOKEN_USAGE("词元使用", "tokenUsage", AgentTokenUsageContentMetadata.class),

    GENERATE_CONVERSATION_NAME("生成会话名称", "generateConversationName", CustomizeContentMetadata.class)

    ;

    private final String name;

    private final String value;

    private final Class<? extends AgentAssistantMessageContent> targetClass;
}
