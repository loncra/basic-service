package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.server.dao.hub.AiMcpPackageDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PackageTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.McpPackageResolver;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * tb_ai_mcp_package 的业务逻辑
 *
 * <p>Table: tb_ai_mcp_package - MCP 连接器目录</p>
 *
 * @see AiMcpPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiMcpPackageService extends BasicService<AiMcpPackageDao, AiMcpPackageEntity> {

    private final List<McpPackageResolver> packageResolvers;

    public List<AiMcpPackageEntity> findSystemDynamicActivationMcpPackage() {
        return lambdaQuery().eq(PluginPackageMetadata::getType, PackageTypeEnum.SYSTEM.getValue())
                .eq(PluginPackageMetadata::getStatus, DataStatusEnum.RELEASE.getValue())
                .eq(AiMcpPackageEntity::getDynamicActivation, YesOrNo.Yes.getValue())
                .list();
    }

    public List<AiMcpPackageEntity> findSystemMcpPackage() {
        return lambdaQuery().eq(PluginPackageMetadata::getType, PackageTypeEnum.SYSTEM.getValue())
                .eq(PluginPackageMetadata::getStatus, DataStatusEnum.RELEASE.getValue())
                .list();
    }

    public Optional<McpClientWrapper> convertMcpClientWrapper(AiMcpPackageEntity mcpPackage) {
        AbstractMcpClientTransportMetadata metadata = mcpPackage.obtainMcpClientTransport();
        return packageResolvers.stream()
                .filter(p -> p.isSupport(metadata.getType()))
                .findFirst()
                .map(s -> s.resolve(mcpPackage.getPackageKey(), metadata));
    }
}
