package io.github.loncra.basic.service.ai.server.service.skill;

import io.github.loncra.basic.service.ai.server.config.SkillConfig;
import io.github.loncra.basic.service.ai.server.resolver.filesystem.SourceOfTruthFileSystemFactory;
import io.github.loncra.basic.service.ai.server.service.hub.AiMcpPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated
public class McpSkillSyncRunner implements ApplicationRunner {

    public static final String SOURCE_PREFIX = "mcpAssociated";

    private final AiMcpPackageService aiMcpPackageService;

    private final SkillConfig skillConfig;

    private final SourceOfTruthFileSystemFactory sourceOfTruthFileSystemFactory;

    @Async
    @Override
    public void run(ApplicationArguments args) {
        /*AbstractFilesystem filesystem = sourceOfTruthFileSystemFactory.getSourceOfTruthFileSystem(skillConfig.getSystemMcpAssociated().getPath());
        List<NoCloseMcpClientWrapper> mcpPackages = aiMcpPackageService.getMcpClientCache()
                .values()
                .stream()
                .filter(s -> NoCloseMcpClientWrapper.class.isAssignableFrom(s.getClass()))
                .map(NoCloseMcpClientWrapper.class::cast)
                .filter(NoCloseMcpClientWrapper::isDynamicActivation)
                .toList();
        for (NoCloseMcpClientWrapper wrapper : mcpPackages) {
            try {
                List<McpSchema.Tool> tools = wrapper.listTools().block(skillConfig.getSystemMcpAssociated().getTimeout().toDuration());

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
                if (filesystem.exists(RuntimeContext.empty(), filePath)) {
                    filesystem.delete(RuntimeContext.empty(), filePath);
                }
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
                .forEach(s -> filesystem.delete(RuntimeContext.empty(), s.path()));*/
    }

    /*public List<AgentSkill> loadAssociatedSkills() {
        AbstractFilesystem filesystem = sourceOfTruthFileSystemFactory
                .getSourceOfTruthFileSystem(skillConfig.getSystemMcpAssociated().getPath());
        GlobResult glob = filesystem.glob(RuntimeContext.empty(), skillConfig.getFilename(), StringUtils.EMPTY);
        if (!glob.isSuccess()) {
            return List.of();
        }
        List<AgentSkill> skills = new ArrayList<>();
        for (FileInfo file : glob.matches()) {
            ReadResult read = filesystem.read(RuntimeContext.empty(), file.path(), 0, 0);
            if (!read.isSuccess() || Objects.isNull(read.fileData())) {
                continue;
            }
            skills.add(SkillUtil.createFrom(read.fileData().content(), Map.of(), SOURCE_PREFIX));
        }
        return skills;
    }*/
}