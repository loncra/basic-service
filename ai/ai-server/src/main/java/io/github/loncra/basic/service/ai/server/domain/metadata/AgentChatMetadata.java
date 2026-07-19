package io.github.loncra.basic.service.ai.server.domain.metadata;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.ai.server.enumerate.agent.AgentChatTypeEnum;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class AgentChatMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 5795604118691592427L;

    /**
     * 会话 id
     */
    private Long agentConversationId;

    /**
     * 应答类型:10.ask,20.plan,30.agent
     */
    @NotNull
    private AgentChatTypeEnum type;
    /**
     * 消息词槽格式内容
     */
    @NotEmpty
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<Map<String, Object>> content = new LinkedList<>();

    /**
     * 附件媒体内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ObjectWriteResult> attachment = new LinkedList<>();

    /**
     * 附加 元数据内容
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
