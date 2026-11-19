package io.github.loncra.basic.service.ai.server.service.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;
import io.agentscope.core.skill.repository.RuntimeContextSkillRepository;
import io.agentscope.core.skill.util.SkillUtil;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.type.SkillUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.agent.AgentConversationEntity;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import io.github.loncra.basic.service.ai.server.domain.metadata.agent.AgentExecutionMetadata;
import io.github.loncra.basic.service.ai.server.resolver.AgentChannelResolver;
import io.github.loncra.basic.service.ai.server.service.hub.AiSkillReleaseService;
import io.github.loncra.basic.service.ai.server.service.hub.AiUserPluginInstallService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

        AgentChannelResolver agentChannelResolver = context.get(AgentChannelResolver.class);
        AgentConversationEntity workspace = agentChannelResolver.getAgentRecordStore().getConversation(
                executionMetadata.getToken().getName(),
                context.getSessionId(),
                executionMetadata.getDeviceIdentified()
        );
        Object workspaceSkillIdsMetadata = workspace.getMetadata()
                .getOrDefault(PluginTargetTypeEnum.SKILL.toString().toLowerCase(), new LinkedList<>());

        LinkedList<SkillUserPluginInstallMetadata> installs = CastUtils.convertValue(
                workspaceSkillIdsMetadata, new TypeReference<>() {}
        );

        List<AiUserPluginInstallEntity> userInstalls = aiUserPluginInstallService.findActivatedInstalls(
                PluginTargetTypeEnum.SKILL,
                executionMetadata.getToken()
        );
        userInstalls.stream()
                .map(s -> CastUtils.convertValue(s.getMetadata(), SkillUserPluginInstallMetadata.class))
                .filter(releaseId -> installs.stream().noneMatch(s -> s.getReleaseId().equals(releaseId.getReleaseId())))
                .forEach(installs::add);

        List<AgentSkill> result = new ArrayList<>();
        for (SkillUserPluginInstallMetadata install : installs) {
            // 安装时钉版的 releaseId（metadata.releaseId，安装流程已写入）→ 读内容
            IdValueMetadata<Long, String> loaded = aiSkillReleaseService.loadSkill(install.getReleaseId());
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
