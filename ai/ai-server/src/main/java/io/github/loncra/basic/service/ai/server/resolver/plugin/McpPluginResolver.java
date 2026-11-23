package io.github.loncra.basic.service.ai.server.resolver.plugin;

import io.agentscope.core.tool.ToolGroup;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.McpUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.basic.service.commons.enumerate.ApiKeyModeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpPluginResolver extends AbstractPluginResolver {

    public static final String PREFIX = "/mcp";

    private final AiMcpPackageService aiMcpPackageService;

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    @Override
    public void applyAgentPluginInstruction(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        try {
            applyInternal(context, hits);
        } catch (RuntimeException e) {
            log.warn("点名 MCP apply 失败", e);
        }
    }

    @Override
    public PluginTargetTypeEnum getTargetType() {
        return PluginTargetTypeEnum.MCP;
    }

    @Override
    public void applyAgentHubInstall(AiUserPluginInstallEntity entity, Map<String, Object> bodyMetadata) {
        entity.setStatus(PluginInstallStatusEnum.ACTIVATED);
        AiMcpPackageEntity mcpEntity =  CastUtils.cast(getPluginPackage(entity.getPackageId()));
        if (!ApiKeyModeEnum.NONE.equals(mcpEntity.getAuthMode())) {
            entity.setStatus(PluginInstallStatusEnum.PENDING);
        }
        McpUserPluginInstallMetadata metadata =  createMcpMetadata(mcpEntity);
        if (ApiKeyModeEnum.MANUAL.equals(mcpEntity.getAuthMode())) {
            metadata.getMetadata().putAll(bodyMetadata);
            AbstractMcpClientTransportMetadata transport = mcpEntity.obtainMetadata().obtainClientTransport(bodyMetadata);
            aiMcpPackageService.createMcpClientWrapper(mcpEntity.getPackageKey(), transport)
                    .ifPresent(mcpClientWrapper -> this.validMcpClient(mcpClientWrapper, entity));

        }
        entity.setMetadata(CastUtils.convertValue(metadata, CastUtils.MAP_TYPE_REFERENCE));
    }

    private void validMcpClient(
            McpClientWrapper mcpClientWrapper,
            AiUserPluginInstallEntity entity
    ) {
        try (McpClientWrapper client = mcpClientWrapper) {
            client.initialize().block();
            entity.setStatus(PluginInstallStatusEnum.ACTIVATED);
        } catch (Exception e) {
            entity.setStatus(PluginInstallStatusEnum.FAILURE);
            entity.getMetadata().put(RestResult.DEFAULT_MESSAGE_NAME, e.getMessage());
        }
    }


    @Override
    public AbstractUserPluginInstallMetadata getAgentHubInstallMetadata(Long packageId) {
        AiMcpPackageEntity entity = CastUtils.cast(getPluginPackage(packageId));
        return createMcpMetadata(entity);
    }

    @Override
    public PluginPackageMetadata getPluginPackage(Long packageId) {
        AiMcpPackageEntity mcpEntity = Objects.requireNonNull(aiMcpPackageService.get(packageId), "找不到 ID 为 [" + packageId + "] 的 [" + getTargetType() + "] 目录");
        assertHubReleased(mcpEntity, getTargetType() + " 目录 [" + mcpEntity.getPackageKey() + "]");
        return mcpEntity;
    }

    private McpUserPluginInstallMetadata createMcpMetadata(AiMcpPackageEntity mcpPackage) {
        McpUserPluginInstallMetadata metadata = new McpUserPluginInstallMetadata();
        if (Objects.nonNull(mcpPackage.getAuthMode())) {
            metadata.setAuthMode(mcpPackage.getAuthMode());
        }
        return metadata;
    }

    private void applyInternal(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        Toolkit toolkit = context.getToolkit();
        if (toolkit == null) {
            return;
        }
        Set<Long> packageIds = new LinkedHashSet<>();
        for (InstructionMessageMetadata hit : hits) {
            Long packageId = parsePackageId(hit);
            if (packageId != null) {
                packageIds.add(packageId);
            }
        }
        List<String> openedNames = new LinkedList<>();
        for (Long packageId : packageIds) {
            try {
                String openedName = activateIfInactive(toolkit, packageId);
                if (StringUtils.isNotBlank(openedName)) {
                    openedNames.add(openedName);
                }
            } catch (RuntimeException e) {
                log.warn("点名 MCP 开组失败 packageId={}", packageId, e);
            }
        }
        if (openedNames.isEmpty()) {
            return;
        }
        context.getMentionSummaries().add("用户点名 MCP: " + String.join("、", openedNames));
    }

    private String activateIfInactive(Toolkit toolkit, Long packageId) {
        AiMcpPackageEntity mcpPackage = aiMcpPackageService.get(packageId);
        if (mcpPackage == null || StringUtils.isBlank(mcpPackage.getPackageKey())) {
            log.warn("点名 MCP 找不到包，跳过 packageId={}", packageId);
            return null;
        }
        String packageKey = mcpPackage.getPackageKey();
        ToolGroup group = toolkit.getToolGroup(packageKey);
        if (group == null) {
            log.warn("点名 MCP 组不存在，跳过 packageId={} packageKey={}", packageId, packageKey);
            return null;
        }
        if (group.isActive()) {
            return null;
        }
        toolkit.updateToolGroups(List.of(packageKey), true);
        log.info("activate mentioned user mcp groups=[{}]", packageKey);
        return StringUtils.defaultIfBlank(mcpPackage.getName(), packageKey);
    }
}
