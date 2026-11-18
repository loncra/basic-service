package io.github.loncra.basic.service.ai.server.service.agent;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;
import io.agentscope.core.skill.repository.RuntimeContextSkillRepository;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;


@Data
@Slf4j
@Component
public class UserPluginRuntimeContextSkillRepository implements RuntimeContextSkillRepository {

    private boolean writeable = true;

    private final AiUserPluginInstallService aiUserPluginInstallService;

    @Override
    public List<AgentSkill> getAllSkills(RuntimeContext context) {
        AgentExecutionMetadata executionMetadata = context.get(AgentExecutionMetadata.class);
        if (Objects.isNull(executionMetadata)) {
            return List.of();
        }
        return aiUserPluginInstallService.findActivatedInstallAgentSkills(
                context.getSessionId(),
                executionMetadata.getToken()
        );

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
                StringUtils.EMPTY,//aiSkillReleaseService.getStorePath(),
                writeable
        );
    }

    @Override
    public String getSource() {
        return AiUserPluginInstallService.SOURCE;
    }
}
