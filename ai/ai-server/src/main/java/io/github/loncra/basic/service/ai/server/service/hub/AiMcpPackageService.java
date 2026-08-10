package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.IdPluginMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PackageTypeEnum;
import io.github.loncra.basic.service.ai.server.dao.hub.AiMcpPackageDao;
import io.github.loncra.basic.service.ai.server.domain.NoCloseMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.resolver.McpPackageResolver;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpPackageService extends BasicService<AiMcpPackageDao, AiMcpPackageEntity> implements InitializingBean, DisposableBean {

    private final List<McpPackageResolver> packageResolvers;

    @Getter
    private final Map<String, McpClientWrapper> mcpClientCache = new ConcurrentHashMap<>();

    @Deprecated
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

    @Override
    public int updateById(AiMcpPackageEntity entity) {
        int result = super.updateById(entity);
        syncMcpClientCache(entity);
        return result;
    }

    private void syncMcpClientCache(AiMcpPackageEntity entity) {
        McpClientWrapper client = initializeThenGetMcpClient(entity);
        if (Objects.nonNull(client)) {
            IdPluginMetadata idPluginMetadata = CastUtils.of(entity,  IdPluginMetadata.class);
            idPluginMetadata.setId(client.getName());
            NoCloseMcpClientWrapper noCloseMcpClientWrapper = new NoCloseMcpClientWrapper(
                    client,
                    idPluginMetadata,
                    entity.getDynamicActivation().toBoolean()
            );
            mcpClientCache.put(noCloseMcpClientWrapper.getName(), noCloseMcpClientWrapper);
        }
    }

    public Optional<McpClientWrapper> convertMcpClientWrapper(AiMcpPackageEntity mcpPackage) {
        AbstractMcpClientTransportMetadata metadata = mcpPackage.obtainMetadata().obtainClientTransport();
        if (Objects.isNull(metadata)) {
            return Optional.empty();
        }
        return packageResolvers.stream()
                .filter(p -> p.isSupport(metadata.getType()))
                .findFirst()
                .map(s -> s.resolve(mcpPackage.getPackageKey(), metadata));
    }

    @Async
    @Override
    public void afterPropertiesSet() throws Exception {
        List<AiMcpPackageEntity> mcpPackages = findSystemMcpPackage();
        for (AiMcpPackageEntity mcpPackage : mcpPackages) {
            try {
                syncMcpClientCache(mcpPackage);
            } catch (Exception e) {
                log.error("MCP {} 同步缓存失败", mcpPackage.getName(), e);
            }
        }
    }

    private McpClientWrapper initializeThenGetMcpClient(AiMcpPackageEntity mcpPackage) {
        Optional<McpClientWrapper> optional = convertMcpClientWrapper(mcpPackage);
        if (optional.isEmpty()) {
            log.warn("MCP {} 解析不出任何 McpClientWrapper", mcpPackage.getName());
            return null;
        }
        McpClientWrapper client = optional.get();
        if (Objects.nonNull(mcpPackage.getInitializeTimeout())) {
            client.initialize().block(mcpPackage.getInitializeTimeout().toDuration());
        } else {
            client.initialize().block();
        }
        client.listTools();
        return client;
    }

    @Override
    public void destroy() throws Exception {
        mcpClientCache.values()
                .stream()
                .filter(s -> NoCloseMcpClientWrapper.class.isAssignableFrom(s.getClass()))
                .map(NoCloseMcpClientWrapper.class::cast)
                .forEach(NoCloseMcpClientWrapper::closeDelegate);
    }
}
