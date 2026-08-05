package io.github.loncra.basic.service.ai.server.service.skill;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.NoCloseMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.metadata.SkillMetadata;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.framework.commons.CastUtils;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpSkillSyncRunner implements ApplicationRunner {

    private final AiMcpPackageService aiMcpPackageService;
    private final SkillConfig skillConfig;

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
        for (NoCloseMcpClientWrapper wrapper : mcpPackages) {
            try {
                List<McpSchema.Tool> tools = wrapper.listTools().block(skillConfig.getTimeout().toDuration());

                SkillMetadata model = new SkillMetadata();
                model.setId(wrapper.getName());
                model.setGroup(wrapper.getGroup());
                model.setTags(wrapper.getTags());
                model.setTools(tools);

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
    }

}
