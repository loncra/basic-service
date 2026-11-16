package io.github.loncra.basic.service.ai.server.service.skill;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.filesystem.AbstractFilesystem;
import io.agentscope.harness.agent.filesystem.model.FileInfo;
import io.agentscope.harness.agent.filesystem.model.LsResult;
import io.agentscope.harness.agent.filesystem.model.WriteResult;
import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.domain.NoCloseMcpClientWrapper;
import io.github.loncra.basic.service.ai.server.domain.metadata.McpSkillGenerateMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.SourceOfTruthFileSystemFactory;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.RegExUtils;
import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.AntPathMatcher;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpSkillSyncRunner implements ApplicationRunner {

    private final AiMcpPackageService aiMcpPackageService;

    private final SkillConfig skillConfig;

    private final SourceOfTruthFileSystemFactory sourceOfTruthFileSystemFactory;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        AbstractFilesystem filesystem = sourceOfTruthFileSystemFactory.getSourceOfTruthFileSystem(skillConfig.getMcp().getAssociatedPath());
        List<NoCloseMcpClientWrapper> mcpPackages = aiMcpPackageService.getMcpClientCache()
                .values()
                .stream()
                .filter(s -> NoCloseMcpClientWrapper.class.isAssignableFrom(s.getClass()))
                .map(NoCloseMcpClientWrapper.class::cast)
                .filter(NoCloseMcpClientWrapper::isDynamicActivation)
                .toList();
        for (NoCloseMcpClientWrapper wrapper : mcpPackages) {
            try {
                List<McpSchema.Tool> tools = wrapper.listTools().block(skillConfig.getMcp().getTimeout().toDuration());

                McpSkillGenerateMetadata model = CastUtils.of(wrapper.getMetadata(), McpSkillGenerateMetadata.class);
                model.setId(wrapper.getName());
                model.setCreationTime(Instant.now());
                model.setTools(tools);
                if (Objects.nonNull(wrapper.getToolClarifyPolicies())) {
                    List<McpClarifyToolPolicyMetadata> enabledPolicies = wrapper.getToolClarifyPolicies()
                            .stream()
                            .filter(s -> s.getEnabled().toBoolean())
                            .toList();

                    if (CollectionUtils.isNotEmpty(enabledPolicies)) {
                        String toolNames = enabledPolicies.stream()
                                .map(McpClarifyToolPolicyMetadata::getToolName)
                                .collect(Collectors.joining(CastUtils.COMMA));
                        model.setClarification(MessageFormat.format(skillConfig.getClarification(), toolNames));
                    }
                }

                Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                configuration.setSharedVariable(CastUtils.getObjectMapper().getClass().getSimpleName(), CastUtils.getObjectMapper());
                Template template = new Template(
                        wrapper.getName(),
                        skillConfig.getContentTemplate(),
                        configuration
                );

                String fullContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                String filePath = wrapper.getName() + AntPathMatcher.DEFAULT_PATH_SEPARATOR + skillConfig.getFilename();
                WriteResult writeResult = filesystem.write(RuntimeContext.empty(), filePath, fullContent);
                if (!writeResult.isSuccess()) {
                    throw new SystemException(writeResult.error());
                }
            } catch (Exception e) {
                log.warn("MCP {} skill 同步失败", wrapper.getName(), e);
            }
        }
        Set<String> activeNames = mcpPackages.stream()
                .map(NoCloseMcpClientWrapper::getName)
                .collect(Collectors.toSet());
        LsResult root = filesystem.ls(RuntimeContext.empty(), StringUtils.EMPTY);
        root.entries().stream()
                .filter(FileInfo::isDirectory)
                .filter(s -> !activeNames.contains(RegExUtils.replaceAll(s.path(), AntPathMatcher.DEFAULT_PATH_SEPARATOR, StringUtils.EMPTY)))
                .forEach(s -> filesystem.delete(RuntimeContext.empty(), s.path()));
    }
}