package io.github.loncra.basic.service.ai.server.service.hub;

import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.BasicPluginMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.type.McpClientSseTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PackageTypeEnum;
import io.github.loncra.basic.service.ai.server.dao.hub.AiMcpPackageDao;
import io.github.loncra.basic.service.ai.server.domain.NoCloseMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.McpPackageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.basic.service.ai.server.resolver.McpPackageResolver;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *
 * tb_ai_mcp_package 的业务逻辑
 *
 * <p>Table: tb_ai_mcp_package - MCP 连接器目录</p>
 *
 * @author maurice.chen
 * @see AiMcpPackageEntity
 * @since 2026-08-04 09:21:08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpPackageService extends BasicService<AiMcpPackageDao, AiMcpPackageEntity> implements InitializingBean, DisposableBean {

    private final List<McpPackageResolver> packageResolvers;

    @Getter
    private final Map<String, McpClientWrapper> mcpClientCache = new ConcurrentHashMap<>();

    public List<McpClarifyToolPolicyMetadata> findMcpClientCacheClarifyToolPolicyMetadata() {
        return streamClarifyWrappers(null)
                .filter(s -> s.getEnabled().toBoolean())
                .toList();
    }

    public Optional<McpClarifyToolPolicyMetadata> getMcpClientCacheClarifyToolPolicyMetadata(
            String mcpName,
            String toolName
    ) {
        return streamClarifyWrappers(mcpName)
                .filter(s -> s.getToolName().equals(toolName))
                .findFirst();
    }

    public List<McpSchema.Tool> remoteTools(
            String name,
            Map<String, Object> client
    ) {
        SystemException.isTrue(MapUtils.isNotEmpty(client), "MCP 配置不能为空");
        AbstractMcpClientTransportMetadata transportMetadata = McpPackageMetadata.obtainClientTransport(client);
        try (McpClientWrapper clientWrapper = createMcpClientWrapper(name, transportMetadata).orElseThrow(() -> new SystemException("无法根据当前传输配置创建 MCP 客户端"))) {
            if (transportMetadata instanceof McpClientSseTransportMetadata sse) {
                initializeClient(clientWrapper, sse.getTimeout());
            } else {
                initializeClient(clientWrapper);
            }
            return clientWrapper.listTools().block();
        }
    }

    public List<AiMcpPackageEntity> findSystemMcpPackage() {
        return lambdaQuery().eq(PluginPackageMetadata::getType, PackageTypeEnum.SYSTEM.getValue())
                .eq(PluginPackageMetadata::getStatus, DataStatusEnum.RELEASE.getValue())
                .list();
    }

    @Override
    public int updateById(AiMcpPackageEntity entity) {
        int result = super.updateById(entity);
        if (DataStatusEnum.RELEASE.equals(entity.getStatus()) && PackageTypeEnum.SYSTEM.equals(entity.getType())) {
            syncMcpClientCache(entity);
        }
        return result;
    }

    private void syncMcpClientCache(AiMcpPackageEntity entity) {
        McpClientWrapper client = initializeThenGetMcpClient(entity);
        if (Objects.nonNull(client)) {
            McpPackageMetadata mcpPackageMetadata = entity.obtainMetadata();
            NoCloseMcpClientWrapper noCloseMcpClientWrapper = new NoCloseMcpClientWrapper(
                    client,
                    CastUtils.of(entity, BasicPluginMetadata.class),
                    mcpPackageMetadata.getClarifyPolicies(),
                    entity.getDynamicActivation().toBoolean()
            );
            mcpClientCache.put(noCloseMcpClientWrapper.getName(), noCloseMcpClientWrapper);
        }
    }

    public Optional<McpClientWrapper> convertMcpClientWrapper(AiMcpPackageEntity mcpPackage) {
        AbstractMcpClientTransportMetadata metadata = mcpPackage.obtainMetadata().obtainClientTransport();
        return createMcpClientWrapper(mcpPackage.getPackageKey(), metadata);
    }

    private @NonNull Optional<McpClientWrapper> createMcpClientWrapper(
            String name,
            AbstractMcpClientTransportMetadata metadata
    ) {
        if (Objects.isNull(metadata)) {
            return Optional.empty();
        }
        return packageResolvers.stream()
                .filter(p -> p.isSupport(metadata.getType()))
                .findFirst()
                .map(s -> s.resolve(name, metadata));
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
        initializeClient(client, mcpPackage.getInitializeTimeout());
        client.listTools();
        return client;
    }

    private Stream<McpClarifyToolPolicyMetadata> streamClarifyWrappers(String mcpName) {
        Stream<NoCloseMcpClientWrapper> stream =  mcpClientCache.values()
                .stream()
                .filter(s -> NoCloseMcpClientWrapper.class.isAssignableFrom(s.getClass()))
                .map(NoCloseMcpClientWrapper.class::cast);
        if (StringUtils.isNotEmpty(mcpName)) {
            stream = stream.filter(s -> s.getName().equals(mcpName));
        }
        return stream.flatMap(s -> s.getToolClarifyPolicies().stream());
    }

    private void initializeClient(
            McpClientWrapper client
    ) {
        initializeClient(client, null);
    }
    private void initializeClient(
            McpClientWrapper client,
            TimeProperties timeout
    ) {
        if (Objects.nonNull(timeout)) {
            client.initialize().block(timeout.toDuration());
        } else {
            client.initialize().block();
        }
    }

    @Override
    public void destroy() throws Exception {
        mcpClientCache.values()
                .stream()
                .filter(s -> NoCloseMcpClientWrapper.class.isAssignableFrom(s.getClass()))
                .map(NoCloseMcpClientWrapper.class::cast)
                .forEach(NoCloseMcpClientWrapper::closeDelegate);
    }

    @Override
    public int insert(AiMcpPackageEntity entity) {
        entity.setStatus(DataStatusEnum.NEW);
        return super.insert(entity);
    }

    public void release(List<Long> ids) {
        get(ids).stream()
                .peek(entity -> entity.setStatus(DataStatusEnum.RELEASE))
                .forEach(this::updateById);
    }

    public void revoke(List<Long> ids) {
        get(ids).stream()
                .peek(entity -> entity.setStatus(DataStatusEnum.REVOKE))
                .forEach(this::updateById);
    }
}
