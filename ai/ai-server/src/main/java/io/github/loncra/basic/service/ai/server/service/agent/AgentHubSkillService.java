package io.github.loncra.basic.service.ai.server.service.agent;

import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.config.AiAppConfig;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillPackageService;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillReleaseService;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 将当前工作空间已激活的 Hub Skill 缓存到 {@code .skill-hub}，并返回第三仓。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentHubSkillService {

    private final AiUserPluginInstallService aiUserPluginInstallService;

    private final AiSkillPackageService aiSkillPackageService;

    private final AiSkillReleaseService aiSkillReleaseService;

    private final AiAppConfig aiAppConfig;

    private final SkillConfig skillConfig;

    public HubSkillRepository ensureCached(String principal, Long workspaceId) {
        Map<String, Path> dirs = new LinkedHashMap<>();
        if (StringUtils.isBlank(principal) || workspaceId == null) {
            return new HubSkillRepository(dirs);
        }
        AgentSkillRepository systemRepo = openSystemRepository();
        List<AiUserPluginInstallEntity> installs = aiUserPluginInstallService.findActivatedInstalls(principal, workspaceId)
                .stream()
                .filter(install -> PluginTargetTypeEnum.SKILL.equals(install.getTargetType()))
                .toList();
        if (CollectionUtils.isEmpty(installs)) {
            return new HubSkillRepository(dirs);
        }
        Map<Long, AiSkillPackageEntity> packages = findSkillPackages(installs);
        for (AiUserPluginInstallEntity install : installs) {
            AiSkillPackageEntity skillPackage = packages.get(install.getPackageId());
            try {
                cacheOne(systemRepo, skillPackage, workspaceId, dirs);
            } catch (RuntimeException e) {
                log.warn("缓存用户 Hub Skill 失败 packageId={}", install.getPackageId(), e);
            }
        }
        return new HubSkillRepository(dirs);
    }

    private void cacheOne(
            AgentSkillRepository systemRepo,
            AiSkillPackageEntity skillPackage,
            Long workspaceId,
            Map<String, Path> dirs
    ) {
        if (skillPackage == null) {
            log.warn("跳过用户 Hub Skill：找不到包");
            return;
        }
        if (!DataStatusEnum.RELEASE.equals(skillPackage.getStatus())) {
            log.warn("跳过未发布用户 Hub Skill: {}", skillPackage.getPackageKey());
            return;
        }
        String packageKey = skillPackage.getPackageKey();
        if (StringUtils.isBlank(packageKey) || packageKey.contains("..")
                || packageKey.contains("/") || packageKey.contains("\\")) {
            log.warn("跳过非法 packageKey 的用户 Hub Skill: {}", packageKey);
            return;
        }
        if (systemRepo != null && systemRepo.skillExists(packageKey)) {
            log.warn("跳过与系统仓同名的用户 Hub Skill: {}", packageKey);
            return;
        }
        List<AiSkillReleaseEntity> releases = aiSkillReleaseService.findEnabledByPackageId(skillPackage.getId());
        if (CollectionUtils.isEmpty(releases)) {
            log.warn("跳过无 enabled release 的用户 Hub Skill: {}", packageKey);
            return;
        }
        AiSkillReleaseEntity latest = releases.getFirst();
        String releaseVersion = latest.getReleaseVersion();
        if (StringUtils.isBlank(releaseVersion) || releaseVersion.contains("/") || releaseVersion.contains("\\")) {
            log.warn("跳过非法 releaseVersion 的用户 Hub Skill: {} version={}", packageKey, releaseVersion);
            return;
        }
        Path dir = Path.of(
                aiAppConfig.getWorkspacePath(),
                String.valueOf(workspaceId),
                ".skill-hub",
                releaseVersion,
                packageKey
        );
        Path skillFile = dir.resolve(skillConfig.getFilename());
        if (Files.isRegularFile(skillFile)) {
            dirs.put(packageKey, dir);
            return;
        }
        try {
            Files.createDirectories(dir);
            aiSkillReleaseService.materialize(latest, dir);
            dirs.put(packageKey, dir);
            log.info("cache hub skill packageKey={} version={}", packageKey, releaseVersion);
        } catch (Exception e) {
            log.warn("下载用户 Hub Skill 失败 packageKey={} version={}", packageKey, releaseVersion, e);
        }
    }

    private AgentSkillRepository openSystemRepository() {
        Path systemPath = Path.of(skillConfig.getPath());
        if (!Files.isDirectory(systemPath)) {
            return null;
        }
        try {
            return new FileSystemSkillRepository(systemPath, false);
        } catch (RuntimeException e) {
            log.warn("打开系统 Skill 仓失败: {}", systemPath, e);
            return null;
        }
    }

    private Map<Long, AiSkillPackageEntity> findSkillPackages(List<AiUserPluginInstallEntity> installs) {
        List<Long> ids = installs.stream()
                .map(AiUserPluginInstallEntity::getPackageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return findSkillPackagesByIds(ids);
    }

    private Map<Long, AiSkillPackageEntity> findSkillPackagesByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Map.of();
        }
        return aiSkillPackageService.lambdaQuery()
                .in(AiSkillPackageEntity::getId, ids)
                .list()
                .stream()
                .filter(item -> Objects.nonNull(item.getId()))
                .collect(Collectors.toMap(AiSkillPackageEntity::getId, item -> item, (left, right) -> left));
    }
}
