package io.github.loncra.basic.service.ai.server.domain.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.agentscope.core.event.AgentEvent;
import io.github.loncra.basic.service.commons.domain.ChatMessageContent;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.codec.ServerSentEvent;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractAgentServerSentEventMetadata extends IdEntity<String> implements ChatMessageContent {

    @Serial
    private static final long serialVersionUID = 2730992177762541731L;

    @JsonIgnore
    private AgentEvent eventSource;

    public ServerSentEvent<String> toServerSentEvent() {
        return ServerSentEvent.<String>builder()
                .id(getId())
                .event(getType())
                .data(SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(this)))
                .build();
    }
}
