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
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;


@Data
@Slf4j
@Component
public class UserPluginRuntimeContextSkillRepository implements RuntimeContextSkillRepository {

    public static final String SOURCE= "hub";

    private boolean writeable = true;

    private final AiUserPluginInstallService aiUserPluginInstallService;

    private final AiSkillReleaseService aiSkillReleaseService;

    @Override
    public List<AgentSkill> getAllSkills(RuntimeContext context) {
        AgentExecutionMetadata executionMetadata = context.get(AgentExecutionMetadata.class);
        if (Objects.isNull(executionMetadata)) {
            return List.of();
        }

        List<AiUserPluginInstallEntity> installs = aiUserPluginInstallService.findActivatedInstalls(
                context.getSessionId(),
                PluginTargetTypeEnum.SKILL,
                executionMetadata.getToken()
        );
        List<AgentSkill> result = new ArrayList<>();
        for (AiUserPluginInstallEntity install : installs) {

            SkillUserPluginInstallMetadata metadata = CastUtils.convertValue(
                    install.getMetadata(), SkillUserPluginInstallMetadata.class);
            // 安装时钉版的 releaseId（metadata.releaseId，安装流程已写入）→ 读内容
            IdValueMetadata<Long, String> loaded = aiSkillReleaseService.loadSkill(metadata.getReleaseId());
            Map<String, String> resources = new LinkedHashMap<>();
            loaded.getMetadata().forEach((key, value) -> resources.put(key, value.toString()));
            AgentSkill agentSkill = SkillUtil.createFrom(loaded.getValue(), resources, SOURCE);

            result.add(agentSkill);
        }
        return result;

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
