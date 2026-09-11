package io.github.loncra.basic.service.ai.server.service.agent;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.api.domain.metadata.BasicPluginMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.McpUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PackageTypeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.NoCloseMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.McpPackageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.McpSkillGenerateMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 将当前用户已激活的 Hub MCP 注册进 Toolkit（组默认 {@code active=false}），
 * 并在工作空间隔离目录写入短伴生 SKILL.md。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentUserPluginToolkitService implements DisposableBean {

    private final AiUserPluginInstallService aiUserPluginInstallService;

    private final AiMcpPackageService aiMcpPackageService;

    private final AiAppConfig aiAppConfig;

    private final SkillConfig skillConfig;

    private final Map<String, McpClientWrapper> userMcpClientCache = new ConcurrentHashMap<>();

    public Path registerUserMcp(
            Toolkit toolkit,
            String principal,
            Long workspaceId
    ) {
        Path userSkillDir = resolveUserSkillDir(workspaceId);
        try {
            Files.createDirectories(userSkillDir);
        } catch (Exception e) {
            log.warn("创建用户 Skill 目录失败: {}", userSkillDir, e);
            return userSkillDir;
        }
        if (toolkit == null || StringUtils.isBlank(principal) || workspaceId == null) {
            return userSkillDir;
        }
        List<AiUserPluginInstallEntity> installs = aiUserPluginInstallService.findActivatedInstalls(principal, workspaceId)
                .stream()
                .filter(install -> PluginTargetTypeEnum.MCP.equals(install.getTargetType()))
                .toList();
        if (CollectionUtils.isEmpty(installs)) {
            return userSkillDir;
        }
        Map<Long, AiMcpPackageEntity> packages = findMcpPackages(installs);
        for (AiUserPluginInstallEntity install : installs) {
            AiMcpPackageEntity mcpPackage = packages.get(install.getPackageId());
            try {
                registerOne(toolkit, principal, install, mcpPackage, userSkillDir);
            } catch (RuntimeException e) {
                log.warn("注册用户 MCP 失败 packageId={}", install.getPackageId(), e);
            }
        }
        return userSkillDir;
    }

    private String registerOne(
            Toolkit toolkit,
            String principal,
            AiUserPluginInstallEntity install,
            AiMcpPackageEntity mcpPackage,
            Path userSkillDir
    ) {
        if (mcpPackage == null) {
            log.warn("跳过用户 MCP：找不到 packageId={}", install.getPackageId());
            return null;
        }
        if (PackageTypeEnum.SYSTEM.equals(mcpPackage.getType())) {
            return null;
        }
        if (!DataStatusEnum.RELEASE.equals(mcpPackage.getStatus())) {
            log.warn("跳过未发布用户 MCP: {}", mcpPackage.getPackageKey());
            return null;
        }
        String packageKey = mcpPackage.getPackageKey();
        if (StringUtils.isBlank(packageKey) || packageKey.contains("..")
                || packageKey.contains("/") || packageKey.contains("\\")) {
            log.warn("跳过非法 packageKey 的用户 MCP: {}", packageKey);
            return null;
        }
        if (Objects.nonNull(toolkit.getToolGroup(packageKey))) {
            log.warn("跳过用户 MCP：工具组已存在 {}", packageKey);
            return null;
        }
        Optional<McpClientWrapper> optional = getOrCreateClient(principal, install, mcpPackage);
        if (optional.isEmpty()) {
            log.warn("用户 MCP {} 解析不出 McpClientWrapper", packageKey);
            return null;
        }
        toolkit.createToolGroup(packageKey, mcpPackage.getName(), false);
        toolkit.registration()
                .mcpClient(optional.get())
                .group(packageKey)
                .apply();
        writeCompanionSkill(userSkillDir, mcpPackage);
        log.info("register user mcp packageKey={} active=false", packageKey);
        return packageKey;
    }

    private Optional<McpClientWrapper> getOrCreateClient(
            String principal,
            AiUserPluginInstallEntity install,
            AiMcpPackageEntity mcpPackage
    ) {
        String cacheKey = cacheKey(principal, install);
        McpClientWrapper cached = userMcpClientCache.get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<McpClientWrapper> converted = aiMcpPackageService.convertMcpClientWrapper(mcpPackage);
        if (converted.isEmpty()) {
            return Optional.empty();
        }
        McpClientWrapper client = converted.get();
        try {
            initializeClient(client, mcpPackage.getInitializeTimeout());
            McpPackageMetadata packageMetadata = mcpPackage.obtainMetadata();
            CachedToolsMcpClientWrapper wrapper = new CachedToolsMcpClientWrapper(
                    client,
                    CastUtils.of(mcpPackage, BasicPluginMetadata.class),
                    packageMetadata == null ? List.of() : packageMetadata.getClarifyPolicies(),
                    dynamicActivation(mcpPackage)
            );
            McpClientWrapper existing = userMcpClientCache.putIfAbsent(cacheKey, wrapper);
            if (existing != null) {
                wrapper.closeDelegate();
                return Optional.of(existing);
            }
            return Optional.of(wrapper);
        } catch (RuntimeException e) {
            client.close();
            throw e;
        }
    }

    private void writeCompanionSkill(Path root, AiMcpPackageEntity mcpPackage) {
        try {
            McpSkillGenerateMetadata model = CastUtils.of(mcpPackage, McpSkillGenerateMetadata.class);
            model.setId(mcpPackage.getPackageKey());
            model.setCreationTime(Instant.now());
            Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configuration.setSharedVariable(CastUtils.getObjectMapper().getClass().getSimpleName(), CastUtils.getObjectMapper());
            Template template = new Template(
                    mcpPackage.getPackageKey(),
                    skillConfig.getContentTemplate(),
                    configuration
            );
            String fullContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            Path dir = root.resolve(mcpPackage.getPackageKey());
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(skillConfig.getFilename()), fullContent, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("用户 MCP {} 伴生 SKILL 写入失败", mcpPackage.getPackageKey(), e);
        }
    }

    private Map<Long, AiMcpPackageEntity> findMcpPackages(List<AiUserPluginInstallEntity> installs) {
        List<Long> ids = installs.stream()
                .map(AiUserPluginInstallEntity::getPackageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return findMcpPackagesByIds(ids);
    }

    private Map<Long, AiMcpPackageEntity> findMcpPackagesByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        return aiMcpPackageService.lambdaQuery()
                .in(AiMcpPackageEntity::getId, ids)
                .list()
                .stream()
                .filter(item -> Objects.nonNull(item.getId()))
                .collect(Collectors.toMap(AiMcpPackageEntity::getId, item -> item, (left, right) -> left));
    }

    private Path resolveUserSkillDir(Long workspaceId) {
        return Path.of(aiAppConfig.getWorkspacePath(), String.valueOf(workspaceId), ".skill-user");
    }

    private String cacheKey(String principal, AiUserPluginInstallEntity install) {
        String credential = StringUtils.EMPTY;
        if (install.obtainMetadata() instanceof McpUserPluginInstallMetadata metadata) {
            credential = StringUtils.defaultString(metadata.getKey());
        }
        return principal + ":" + install.getPackageId() + ":" + credential;
    }

    private boolean dynamicActivation(AiMcpPackageEntity mcpPackage) {
        YesOrNo dynamicActivation = mcpPackage.getDynamicActivation();
        return dynamicActivation == null || dynamicActivation.toBoolean();
    }

    private void initializeClient(McpClientWrapper client, TimeProperties timeout) {
        if (Objects.nonNull(timeout)) {
            client.initialize().block(timeout.toDuration());
        } else {
            client.initialize().block();
        }
    }

    @Override
    public void destroy() {
        userMcpClientCache.values()
                .stream()
                .filter(CachedToolsMcpClientWrapper.class::isInstance)
                .map(CachedToolsMcpClientWrapper.class::cast)
                .forEach(NoCloseMcpClientWrapper::closeDelegate);
        userMcpClientCache.clear();
    }

    private static final class CachedToolsMcpClientWrapper extends NoCloseMcpClientWrapper {

        private volatile List<McpSchema.Tool> cachedTools;

        private CachedToolsMcpClientWrapper(
                McpClientWrapper delegate,
                BasicPluginMetadata metadata,
                List<McpClarifyToolPolicyMetadata> toolClarifyPolicies,
                boolean dynamicActivation
        ) {
            super(delegate, metadata, toolClarifyPolicies, dynamicActivation);
        }

        @Override
        public Mono<List<McpSchema.Tool>> listTools() {
            if (cachedTools != null) {
                return Mono.just(cachedTools);
            }
            return super.listTools().doOnSuccess(tools -> cachedTools = tools);
        }
    }
}
