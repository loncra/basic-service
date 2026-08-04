package io.github.loncra.basic.service.ai.server.service.skill;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.agentscope.core.tool.mcp.McpClientWrapper;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiMcpPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpSkillSyncRunner implements ApplicationRunner {

    private final AiMcpPackageService aiMcpPackageService;
    private final SkillConfig skillConfig;

    @Override
    public void run(ApplicationArguments args) {
        Path root = Path.of(skillConfig.getPath());
        List<AiMcpPackageEntity> mcpPackages = aiMcpPackageService.findSystemDynamicActivationMcpPackage();
        for (AiMcpPackageEntity mcpPackage : mcpPackages) {
            Optional<McpClientWrapper> optional = aiMcpPackageService.convertMcpClientWrapper(mcpPackage);
            if (optional.isEmpty()) {
                log.warn("MCP {} skill 同步失败，解析不出任何 McpClientWrapper", mcpPackage.getName());
                continue;
            }
            try (McpClientWrapper client = optional.get()) {
                client.initialize().block(skillConfig.getTimeout().toDuration());
                List<McpSchema.Tool> tools = client.listTools().block(skillConfig.getTimeout().toDuration());

                SkillMetadata model = new SkillMetadata();
                model.setId(client.getName());
                model.setGroup(mcpPackage.getGroup());
                model.setTools(tools);

                Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                configuration.setSharedVariable(CastUtils.getObjectMapper().getClass().getSimpleName(), CastUtils.getObjectMapper());
                Template template = new Template(
                        client.getName(),
                        skillConfig.getContentTemplate(),
                        configuration
                );

                String fullContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

                Path dir = root.resolve(client.getName());
                Files.createDirectories(dir);
                Files.writeString(dir.resolve(skillConfig.getFilename()), fullContent, StandardCharsets.UTF_8);
            } catch (Exception e) {
                // 失败保留旧 SKILL.md，打日志，不要拖死启动
                log.warn("MCP {} skill 同步失败", mcpPackage.getName(), e);
            }
        }
    }

}
