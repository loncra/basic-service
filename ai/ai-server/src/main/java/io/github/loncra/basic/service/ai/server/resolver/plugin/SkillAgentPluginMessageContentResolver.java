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
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentMessageEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentPluginInstructionContext;
import io.github.loncra.basic.service.ai.server.resolver.AgentPluginMessageContentResolver;
import io.github.loncra.basic.service.ai.server.service.agent.HubSkillRepository;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillPackageService;
import io.github.loncra.basic.service.commons.domain.metadata.chat.InstructionMessageMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkillAgentPluginMessageContentResolver implements AgentPluginMessageContentResolver {

    public static final String PREFIX = "/skill";

    private static final String SKILL_FILE = "SKILL.md";

    private final AiSkillPackageService aiSkillPackageService;

    @Override
    public List<InstructionMessageMetadata> getHits(AgentMessageEntity userMessage) {
        if (userMessage == null) {
            return List.of();
        }
        return InstructionMessageMetadata.ofList(userMessage.getContent()).stream()
                .filter(s -> Objects.equals(PREFIX, s.getPrefix()))
                .toList();
    }

    @Override
    public void apply(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        try {
            applyInternal(context, hits);
        } catch (RuntimeException e) {
            log.warn("点名 Skill apply 失败", e);
        }
    }

    private void applyInternal(AgentPluginInstructionContext context, List<InstructionMessageMetadata> hits) {
        if (context == null || CollectionUtils.isEmpty(hits)) {
            return;
        }
        HubSkillRepository hubSkillRepository = findHubSkillRepository(context.getAgent());
        if (hubSkillRepository == null) {
            log.warn("点名 Skill 找不到 Hub 仓，跳过");
            return;
        }
        Set<Long> packageIds = new LinkedHashSet<>();
        for (InstructionMessageMetadata hit : hits) {
            Long packageId = parsePackageId(hit);
            if (packageId != null) {
                packageIds.add(packageId);
            }
        }
        List<String> loadedNames = new LinkedList<>();
        for (Long packageId : packageIds) {
            try {
                String loadedName = loadIfPresent(context, hubSkillRepository, packageId);
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
            HubSkillRepository hubSkillRepository,
            Long packageId
    ) {
        AiSkillPackageEntity skillPackage = aiSkillPackageService.get(packageId);
        if (skillPackage == null || StringUtils.isBlank(skillPackage.getPackageKey())) {
            log.warn("点名 Skill 找不到包，跳过 packageId={}", packageId);
            return null;
        }
        String packageKey = skillPackage.getPackageKey();
        if (!hubSkillRepository.skillExists(packageKey)) {
            log.warn("点名 Skill 不在 Hub 仓，跳过 packageId={} packageKey={}", packageId, packageKey);
            return null;
        }
        AgentSkill skill = hubSkillRepository.getSkill(packageKey);
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
                "path", SKILL_FILE
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

    private HubSkillRepository findHubSkillRepository(HarnessAgent agent) {
        if (agent == null || CollectionUtils.isEmpty(agent.getSkillRepositories())) {
            return null;
        }
        for (AgentSkillRepository repository : agent.getSkillRepositories()) {
            if (repository instanceof HubSkillRepository hubSkillRepository) {
                return hubSkillRepository;
            }
        }
        return null;
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
