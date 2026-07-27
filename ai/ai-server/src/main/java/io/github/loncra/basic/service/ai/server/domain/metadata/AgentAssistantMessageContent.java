package io.github.loncra.basic.service.ai.server.domain.metadata;

import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.codec.ServerSentEvent;

import java.io.Serial;

/**
 * Agent 助手消息 content 块基类：id + lastSseEventId。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AgentAssistantMessageContent extends AgentServerSentEventMessage {

    @Serial
    private static final long serialVersionUID = 8121665348839272979L;

    /**
     * 该块落库/推流对应的最后一条开始重放推流的 id。
     */
    private String sseEventId;

    public ServerSentEvent<String> toServerSentEvent() {
        return ServerSentEvent.<String>builder()
                .id(sseEventId)
                .event(getType())
                .data(SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(this)))
                .build();
    }
}
