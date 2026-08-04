package io.github.loncra.basic.service.ai.server.enumerate;

import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.mcp.McpClientSseTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.mcp.McpClientStdioTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.mcp.McpClientStreamableHttpTransportMetadata;
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
