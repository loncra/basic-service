package io.github.loncra.basic.service.ai.server.resolver.plugin;

import io.agentscope.core.tool.ToolGroup;
import io.agentscope.core.tool.Toolkit;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.ai.server.resolver.AbstractAgentPluginMessageContentResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpAgentPluginMessageContentResolver extends AbstractAgentPluginMessageContentResolver {

    public static final String PREFIX = "/mcp";

    private final AiMcpPackageService aiMcpPackageService;

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    @Override
    public void apply(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        try {
            applyInternal(context, hits);
        } catch (RuntimeException e) {
            log.warn("点名 MCP apply 失败", e);
        }
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

    private Long parsePackageId(InstructionMessageMetadata hit) {
        if (hit == null) {
            return null;
        }
        IdValueMetadata<String, String> value = hit.getValue();
        if (value == null || StringUtils.isBlank(value.getId())) {
            return null;
        }
        String id = value.getId().trim();
        if (!NumberUtils.isDigits(id)) {
            return null;
        }
        long packageId = NumberUtils.toLong(id);
        return packageId > 0 ? packageId : null;
    }
}
