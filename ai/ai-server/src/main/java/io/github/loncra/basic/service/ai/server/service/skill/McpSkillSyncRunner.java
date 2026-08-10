package io.github.loncra.basic.service.ai.server.service.skill;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.constants.SkillConstants;
import io.github.loncra.basic.service.ai.server.domain.NoCloseMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.SkillMetadata;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.framework.commons.CastUtils;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpSkillSyncRunner implements ApplicationRunner {

    private final AiMcpPackageService aiMcpPackageService;
    private final SkillConfig skillConfig;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        Path root = Path.of(skillConfig.getPath());
        List<NoCloseMcpClientWrapper> mcpPackages = aiMcpPackageService.getMcpClientCache()
                .values()
                .stream()
                .filter(s -> NoCloseMcpClientWrapper.class.isAssignableFrom(s.getClass()))
                .map(NoCloseMcpClientWrapper.class::cast)
                .filter(NoCloseMcpClientWrapper::isDynamicActivation)
                .toList();
        Map<String, AiMcpPackageEntity> packageByKey = aiMcpPackageService.findSystemMcpPackage().stream()
                .filter(pkg -> StringUtils.isNotBlank(pkg.getPackageKey()))
                .collect(Collectors.toMap(AiMcpPackageEntity::getPackageKey, pkg -> pkg, (a, b) -> a));

        for (NoCloseMcpClientWrapper wrapper : mcpPackages) {
            try {
                List<McpSchema.Tool> tools = wrapper.listTools().block(skillConfig.getTimeout().toDuration());

                SkillMetadata model = new SkillMetadata();
                model.setId(wrapper.getName());
                model.setGroup(wrapper.getGroup());
                model.setTags(wrapper.getTags());
                model.setTools(tools);
                model.setCreationTime(Instant.now());
                resolveSkillGuidance(packageByKey.get(wrapper.getName())).ifPresent(model::setGuidance);

                Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                configuration.setSharedVariable(CastUtils.getObjectMapper().getClass().getSimpleName(), CastUtils.getObjectMapper());
                Template template = new Template(
                        wrapper.getName(),
                        skillConfig.getContentTemplate(),
                        configuration
                );

                String fullContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

                Path dir = root.resolve(wrapper.getName());
                Files.createDirectories(dir);
                Files.writeString(dir.resolve(skillConfig.getFilename()), fullContent, StandardCharsets.UTF_8);
            } catch (Exception e) {
                // 失败保留旧 SKILL.md，打日志，不要拖死启动
                log.warn("MCP {} skill 同步失败", wrapper.getName(), e);
            }
        }
        Set<String> activeNames = mcpPackages.stream()
                .map(NoCloseMcpClientWrapper::getName)
                .collect(Collectors.toSet());
        cleanObsoleteSkillDirs(root, activeNames);
    }

    /**
     * 读取 {@code AiMcpPackageEntity.metadata.skill.guidance}
     */
    private Optional<String> resolveSkillGuidance(AiMcpPackageEntity mcpPackage) {
        if (Objects.isNull(mcpPackage) || MapUtils.isEmpty(mcpPackage.getMetadata())) {
            return Optional.empty();
        }
        Map<String, Object> skill = CastUtils.convertValue(
                mcpPackage.getMetadata().get(SkillConstants.METADATA_SKILL_KEY),
                CastUtils.MAP_TYPE_REFERENCE
        );
        if (MapUtils.isEmpty(skill)) {
            return Optional.empty();
        }
        String guidance = Objects.toString(skill.get(SkillConstants.SKILL_GUIDANCE), null);
        return StringUtils.isBlank(guidance) ? Optional.empty() : Optional.of(guidance.trim());
    }

    /**
     * 删除 root 下不在 activeNames 中的 MCP skill 子目录。
     */
    private void cleanObsoleteSkillDirs(Path root, Set<String> activeNames) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (var dirs = Files.list(root)) {
            dirs.filter(Files::isDirectory)
                    .filter(dir -> !activeNames.contains(dir.getFileName().toString()))
                    .forEach(this::deleteQuietly);
        } catch (IOException e) {
            log.warn("扫描 skill 目录失败，跳过清理", e);
        }
    }

    /**
     * 递归删除目录，不抛异常。
     */
    private void deleteQuietly(Path dir) {
        try (var walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {}", path, e);
                        }
                    });
            log.info("已清理禁用的 MCP skill 目录: {}", dir.getFileName());
        } catch (IOException e) {
            log.warn("清理目录失败: {}", dir, e);
        }
    }
}
