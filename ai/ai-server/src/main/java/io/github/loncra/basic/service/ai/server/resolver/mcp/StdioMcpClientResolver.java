package io.github.loncra.basic.service.ai.server.resolver.mcp;

import io.agentscope.core.tool.mcp.McpClientBuilder;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.ContentAwareMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.mcp.McpClientStdioTransportMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.McpPackageResolver;
import io.github.loncra.framework.commons.CastUtils;
import org.springframework.stereotype.Component;

@Component
public class StdioMcpClientResolver implements McpPackageResolver {

    @Override
    public boolean isSupport(String type) {
        return McpClientTypeEnum.SSE.toString().equals(type);
    }

    @Override
    public McpClientWrapper resolve(
            String name,
            AbstractMcpClientTransportMetadata metadata
    ) {
        McpClientStdioTransportMetadata stdio = CastUtils.cast(metadata);
        McpClientWrapper clientWrapper = McpClientBuilder.create(name)
                .stdioTransport(stdio.getCommand(), stdio.getArgs(), stdio.getEnv())
                .buildSync();
        return new ContentAwareMcpClientWrapper(clientWrapper, stdio.getFailureAssertions());
    }
}
