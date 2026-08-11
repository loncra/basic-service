package io.github.loncra.basic.service.ai.api.enumerate;

import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type.McpClientSseTransportMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type.McpClientStdioTransportMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type.McpClientStreamableHttpTransportMetadata;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum McpClientTypeEnum implements NameEnum {

    SSE("sse", McpClientSseTransportMetadata.class ),

    STDIO("stdio", McpClientStdioTransportMetadata.class),

    STREAMABLE_HTTP("streamableHttp", McpClientStreamableHttpTransportMetadata.class),
    ;

    private final String name;

    private final Class<? extends AbstractMcpClientTransportMetadata> targetClass;
}
