package io.github.loncra.basic.service.ai.server.resolver.plugin;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolResultState;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.skill.runtime.HarnessSkillEntry;
import io.agentscope.harness.agent.skill.runtime.SkillCatalog;
import io.agentscope.harness.agent.skill.runtime.SkillLoadTool;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.SkillUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.ai.server.service.agent.UserPluginRuntimeContextSkillRepository;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillPackageService;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillReleaseService;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillPluginResolver extends AbstractPluginResolver {

    public static final String PREFIX = "/skill";

    private final AiSkillPackageService aiSkillPackageService;

    private final AiSkillReleaseService aiSkillReleaseService;

    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    @Override
    public void applyAgentPluginInstruction(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        try {
            applyInternal(context, hits);
        } catch (RuntimeException e) {
            log.warn("点名 Skill apply 失败", e);
        }
    }

    @Override
    public PluginTargetTypeEnum getTargetType() {
        return PluginTargetTypeEnum.SKILL;
    }

    @Override
    public void applyAgentHubInstall(AiUserPluginInstallEntity entity, Map<String, Object> bodyMetadata) {
        entity.setStatus(PluginInstallStatusEnum.ACTIVATED);
        AbstractUserPluginInstallMetadata metadata = getAgentHubInstallMetadata(entity.getPackageId(), bodyMetadata);
        entity.setMetadata(CastUtils.convertValue(metadata, CastUtils.MAP_TYPE_REFERENCE));
    }

    @Override
    public AbstractUserPluginInstallMetadata getAgentHubInstallMetadata(Long packageId) {
        return getAgentHubInstallMetadata(packageId, Map.of());
    }

    @Override
    public PluginPackageMetadata getPluginPackage(Long packageId) {
        AiSkillPackageEntity entity = Objects.requireNonNull(aiSkillPackageService.get(packageId),"找不到 ID 为 [" + packageId + "] 的 [" + getTargetType() + "] 目录");
        assertHubReleased(entity, getTargetType() + " 目录 [" + entity.getPackageKey() + "]");
        return entity;
    }

    /**
     * 按 bodyMetadata.releaseVersion 锁定版本；未指定时回退目录 latestVersion。
     * 指定版本不存在或未启用直接报错，不静默回退最新。
     */
    private AbstractUserPluginInstallMetadata getAgentHubInstallMetadata(Long packageId, Map<String, Object> bodyMetadata) {
        AiSkillPackageEntity entity = CastUtils.cast(getPluginPackage(packageId));
        SystemException.isTrue(
                StringUtils.isNotBlank(entity.getLatestVersion()),
                () -> new ServiceException("Skill 目录 [" + entity.getPackageKey() + "] 尚未打包版本，不能安装")
        );
        Object requested = Objects.isNull(bodyMetadata) ? null : bodyMetadata.get("releaseVersion");
        String releaseVersion = StringUtils.isNotBlank(Objects.toString(requested, null))
                ? Objects.toString(requested)
                : entity.getLatestVersion();
        return createSkillMetadata(entity, releaseVersion);
    }

    private SkillUserPluginInstallMetadata createSkillMetadata(AiSkillPackageEntity skillPackage, String releaseVersion) {
        AiSkillReleaseEntity release = aiSkillReleaseService.lambdaQuery()
                .eq(AiSkillReleaseEntity::getAiSkillPackageId, skillPackage.getId())
                .eq(AiSkillReleaseEntity::getReleaseVersion, releaseVersion)
                .eq(AiSkillReleaseEntity::getEnabled, YesOrNo.Yes)
                .one();
        SystemException.isTrue(
                Objects.nonNull(release),
                () -> new ServiceException("Skill 目录 [" + skillPackage.getPackageKey() + "] 找不到已启用的版本 [" + releaseVersion + "]")
        );
        SkillUserPluginInstallMetadata metadata = new SkillUserPluginInstallMetadata();
        metadata.setReleaseId(release.getId());
        metadata.setReleaseVersion(release.getReleaseVersion());
        metadata.setContentHash(release.getContentHash());
        metadata.setUpdatePolicy(skillPackage.getDefaultUpdatePolicy());
        return metadata;
    }

    private void applyInternal(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        if (context == null || CollectionUtils.isEmpty(hits)) {
            return;
        }
        UserPluginRuntimeContextSkillRepository userPluginRuntimeContextSkillRepository = findHubSkillRepository(context.getAgent());
        if (userPluginRuntimeContextSkillRepository == null) {
            log.warn("点名 Skill 找不到 Hub 仓，跳过");
            return;
        }
        Set<Long> packageIds = getPackageIds(hits);
        List<String> loadedNames = new LinkedList<>();
        for (Long packageId : packageIds) {
            try {
                String loadedName = loadIfPresent(context, userPluginRuntimeContextSkillRepository, packageId);
                if (StringUtils.isNotBlank(loadedName)) {
                    loadedNames.add(loadedName);
                }
            } catch (RuntimeException e) {
                log.warn("点名 Skill load 失败 packageId={}", packageId, e);
            }
        }
        if (loadedNames.isEmpty()) {
            return;
        }
        context.getMentionSummaries().add("用户点名 Skill: " + String.join("、", loadedNames));
    }

    private String loadIfPresent(
            AgentPluginInstructionContext context,
            UserPluginRuntimeContextSkillRepository userPluginRuntimeContextSkillRepository,
            Long packageId
    ) {
        AiSkillPackageEntity skillPackage = aiSkillPackageService.get(packageId);
        if (skillPackage == null || StringUtils.isBlank(skillPackage.getPackageKey())) {
            log.warn("点名 Skill 找不到包，跳过 packageId={}", packageId);
            return null;
        }
        String packageKey = skillPackage.getPackageKey();
        if (!userPluginRuntimeContextSkillRepository.skillExists(packageKey)) {
            log.warn("点名 Skill 不在 Hub 仓，跳过 packageId={} packageKey={}", packageId, packageKey);
            return null;
        }
        AgentSkill skill = userPluginRuntimeContextSkillRepository.getSkill(packageKey);
        if (skill == null || StringUtils.isBlank(skill.getSkillContent())) {
            log.warn("点名 Skill 读仓失败，跳过 packageId={} packageKey={}", packageId, packageKey);
            return null;
        }
        if (!invokeLoadSkillThroughPath(context, skill)) {
            return null;
        }
        log.info("load mentioned hub skill packageKey={}", packageKey);
        return StringUtils.defaultIfBlank(skillPackage.getName(), packageKey);
    }

    /**
     * 等价本轮已 {@code load_skill_through_path(skillId, SKILL.md)}。
     * apply 早于 {@code onSystemPrompt}，需先用仓里的 skill 装一帧 {@link SkillCatalog}。
     */
    private boolean invokeLoadSkillThroughPath(AgentPluginInstructionContext context, AgentSkill skill) {
        Toolkit toolkit = context.getToolkit();
        if (toolkit == null && context.getAgent() != null) {
            toolkit = context.getAgent().getToolkit();
        }
        if (toolkit == null) {
            log.warn("点名 Skill 没有 Toolkit，跳过 packageKey={}", skill.getName());
            return false;
        }
        AgentTool tool = toolkit.getTool(SkillLoadTool.TOOL_NAME);
        if (tool == null) {
            log.warn("点名 Skill 找不到 {}，跳过 packageKey={}", SkillLoadTool.TOOL_NAME, skill.getName());
            return false;
        }
        SkillCatalog catalog = SkillCatalog.of(List.of(HarnessSkillEntry.of(skill, null)));
        RuntimeContext runtimeContext = RuntimeContext.builder()
                .put(SkillCatalog.class, catalog)
                .build();
        Map<String, Object> input = Map.of(
                "skillId", skill.getSkillId(),
                "path", aiSkillPackageService.getSkillConfig().getFilename()
        );
        ToolCallParam param = ToolCallParam.builder()
                .toolUseBlock(ToolUseBlock.builder()
                        .id("skill-mention-" + skill.getName())
                        .name(SkillLoadTool.TOOL_NAME)
                        .input(input)
                        .build())
                .input(input)
                .agent(context.getAgent())
                .runtimeContext(runtimeContext)
                .build();
        ToolResultBlock result = tool.callAsync(param).block();
        if (result == null || ToolResultState.ERROR.equals(result.getState())) {
            log.warn("点名 Skill load_skill_through_path 失败 packageKey={}", skill.getName());
            return false;
        }
        return true;
    }

    private UserPluginRuntimeContextSkillRepository findHubSkillRepository(HarnessAgent agent) {
        if (agent == null || CollectionUtils.isEmpty(agent.getSkillRepositories())) {
            return null;
        }
        for (AgentSkillRepository repository : agent.getSkillRepositories()) {
            if (repository instanceof UserPluginRuntimeContextSkillRepository userPluginRuntimeContextSkillRepository) {
                return userPluginRuntimeContextSkillRepository;
            }
        }
        return null;
    }
}
