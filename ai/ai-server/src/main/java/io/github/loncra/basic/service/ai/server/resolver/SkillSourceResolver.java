package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;

import java.nio.file.Path;

/**
 * Skill 来源物化（对标 {@link McpPackageResolver}）
 *
 * @author maurice.chen
 */
public interface SkillSourceResolver {

    boolean isSupport(String type);

    /**
     * 将目录来源物化为本地 Skill 目录树（含 SKILL.md）。
     * 各类型实现未就绪时抛出 {@link io.github.loncra.framework.commons.exception.SystemException}。
     */
    Path materialize(String packageKey, AbstractSkillSourceMetadata metadata);
}
