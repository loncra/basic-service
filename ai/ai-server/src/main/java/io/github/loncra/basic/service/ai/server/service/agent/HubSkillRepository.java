package io.github.loncra.basic.service.ai.server.service.agent;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;
import io.agentscope.core.skill.util.SkillFileSystemHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.*;

/**
 * 只读 Hub Skill 仓：对外 id 为 {@code packageKey}，目录来自 {@code .skill-hub} 本机缓存。
 */
@Slf4j
public class HubSkillRepository implements AgentSkillRepository {

    public static final String SOURCE = "hub-minio-cache";

    private final Map<String, Path> dirsByPackageKey;

    private boolean writeable;

    public HubSkillRepository(Map<String, Path> dirsByPackageKey) {
        this.dirsByPackageKey = dirsByPackageKey == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(dirsByPackageKey));
        this.writeable = false;
    }

    @Override
    public AgentSkill getSkill(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        Path dir = dirsByPackageKey.get(name);
        if (dir == null) {
            return null;
        }
        try {
            return load(name, dir);
        } catch (RuntimeException e) {
            log.warn("加载 Hub Skill 失败 packageKey={}", name, e);
            return null;
        }
    }

    @Override
    public List<String> getAllSkillNames() {
        return new ArrayList<>(dirsByPackageKey.keySet());
    }

    @Override
    public List<AgentSkill> getAllSkills() {
        List<AgentSkill> skills = new ArrayList<>();
        for (Map.Entry<String, Path> entry : dirsByPackageKey.entrySet()) {
            try {
                skills.add(load(entry.getKey(), entry.getValue()));
            } catch (RuntimeException e) {
                log.warn("加载 Hub Skill 失败 packageKey={}", entry.getKey(), e);
            }
        }
        return skills;
    }

    @Override
    public boolean save(List<AgentSkill> skills, boolean force) {
        return false;
    }

    @Override
    public boolean delete(String skillName) {
        return false;
    }

    @Override
    public boolean skillExists(String skillName) {
        return StringUtils.isNotBlank(skillName) && dirsByPackageKey.containsKey(skillName);
    }

    @Override
    public AgentSkillRepositoryInfo getRepositoryInfo() {
        return new AgentSkillRepositoryInfo(SOURCE, SOURCE, false);
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public void setWriteable(boolean writeable) {
        this.writeable = false;
    }

    @Override
    public boolean isWriteable() {
        return writeable;
    }

    private AgentSkill load(String packageKey, Path dir) {
        AgentSkill loaded = SkillFileSystemHelper.loadSkillFromDirectory(dir, getSource(), false);
        return loaded.toBuilder().name(packageKey).build();
    }
}
