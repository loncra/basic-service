package io.github.loncra.basic.service.ai.server.service.agent;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;
import io.agentscope.core.skill.repository.RuntimeContextSkillRepository;
import io.agentscope.core.skill.util.SkillUtil;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.SkillUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillReleaseService;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.basic.service.ai.server.service.skill.McpSkillSyncRunner;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;


@Data
@Slf4j
@Component
public class HubSkillRepository implements RuntimeContextSkillRepository {

    public static final String SOURCE = "hub";

    private boolean writeable = true;

    private final AiSkillReleaseService aiSkillReleaseService;

    private final McpSkillSyncRunner mcpSkillSyncRunner;

    private final AiUserPluginInstallService aiUserPluginInstallService;

    @Override
    public List<AgentSkill> getAllSkills(RuntimeContext context) {
        AgentExecutionMetadata executionMetadata = context.get(AgentExecutionMetadata.class);
        if (Objects.isNull(executionMetadata)) {
            return List.of();
        }

        // 系统级 MCP 伴生（默认可见，无需安装）
        List<AgentSkill> skills = new ArrayList<>(mcpSkillSyncRunner.loadAssociatedSkills());
        List<AiUserPluginInstallEntity> installs = aiUserPluginInstallService.findActivatedInstalls(
                context.getSessionId(),
                executionMetadata.getToken()
        );
        // 该工作空间生效的安装（scope=USER 全生效 + scope=WORKSPACE 仅绑定空间）
        for (AiUserPluginInstallEntity install : installs) {
            try {
                if (PluginTargetTypeEnum.SKILL.equals(install.getTargetType())) {
                    skills.add(createAgentSkill(install));
                } else if (PluginTargetTypeEnum.MCP.equals(install.getTargetType())) {

                }
            } catch (Exception e) {
                log.warn("加载已安装插件 [{}] 失败，跳过", install.getId(), e);   // 单包失败不拖垮整仓
            }
        }
        return skills;
    }

    private AgentSkill createAgentSkill(AiUserPluginInstallEntity install) {
        SkillUserPluginInstallMetadata metadata = CastUtils.convertValue(
                install.getMetadata(), SkillUserPluginInstallMetadata.class);
        // 安装时钉版的 releaseId（metadata.releaseId，安装流程已写入）→ 读内容
        IdValueMetadata<Long, String> loaded = aiSkillReleaseService.loadSkill(metadata.getReleaseId());
        Map<String, String> resources = new LinkedHashMap<>();
        loaded.getMetadata().forEach((key, value) -> resources.put(key, value.toString()));
        return SkillUtil.createFrom(loaded.getValue(), resources, SOURCE);
    }

    @Override
    public AgentSkill getSkill(String name) {
        return null;
    }

    @Override
    public List<String> getAllSkillNames() {
        return List.of();
    }

    @Override
    public List<AgentSkill> getAllSkills() {
        return List.of();
    }

    @Override
    public boolean save(
            List<AgentSkill> skills,
            boolean force
    ) {
        return false;
    }

    @Override
    public boolean delete(String skillName) {
        return false;
    }

    @Override
    public boolean skillExists(String skillName) {
        return false;
    }

    @Override
    public AgentSkillRepositoryInfo getRepositoryInfo() {
        return new AgentSkillRepositoryInfo(
                getSource(),
                aiSkillReleaseService.getStorePath(),
                writeable
        );
    }

    @Override
    public String getSource() {
        return SOURCE;
    }
}
