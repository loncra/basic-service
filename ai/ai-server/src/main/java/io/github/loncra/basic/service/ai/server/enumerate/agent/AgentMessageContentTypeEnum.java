package io.github.loncra.basic.service.ai.server.enumerate.agent;


import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractAssistantMessageContentMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.content.*;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentMessageContentTypeEnum implements NameValueEnum<String> {

    THINK("思考", "think", ThinkBlockContentMetadata.class),

    TOOL_CALL("执行工具", "tool", ToolCallBlockContentMetadata.class),

    ANSWER("回答", "answer", AnswerBlockContentMetadata.class),

    ERROR("错误", "error", CustomizeMetadata.class),

    AGENT_STATUS_CHANGE("智能体状态变更", "agentStatusChange", AgentStatusContentMetadata.class),

    STREAM_START("流开始", "streamStart", CustomizeMetadata.class),

    STREAM_END("流结束", "streamEnd", CustomizeMetadata.class),

    TOKEN_USAGE("词元使用", "tokenUsage", AgentTokenUsageMetadata.class),

    GENERATE_CONVERSATION_NAME("生成会话名称", "generateConversationName", CustomizeMetadata.class),

    MODEL_CALL_END("模型调用", "modelCallEnd", AgentTokenUsageMetadata.class)
    ;

    private final String name;

    private final String value;

    private final Class<? extends AbstractAssistantMessageContentMetadata> targetClass;
}
