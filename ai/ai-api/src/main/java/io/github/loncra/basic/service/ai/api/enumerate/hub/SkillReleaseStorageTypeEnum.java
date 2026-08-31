package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.release.GitTreeSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.release.InlineSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.release.ObjectSkillReleaseStorageMetadata;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Skill 不可变版本（Release）的内容落点。
 * <p>
 * 与 {@link SkillSourceTypeEnum} 不同：来源回答「下次去哪拉」，本枚举回答「这一版文件实际存在哪」。
 * 用户安装锁的是 Release，正式广场应落到 {@link #OBJECT}。
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SkillReleaseStorageTypeEnum implements NameValueEnum<Integer> {

    /**
     * 对象存储：把该版本完整目录树（SKILL.md 及 scripts/references/assets 等）拷到 MinIO 前缀下。
     * 安装与运行都读这棵树，不回头去拉 git 或 zip。
     */
    OBJECT("对象存储", 10, ObjectSkillReleaseStorageMetadata.class),

    /**
     * Git 树：记录 fetch 当时的仓库 url、commit sha、子目录 path，作审计/溯源。
     * 不是用户安装路径；打快照后内容仍应落到 {@link #OBJECT}。
     */
    GIT_TREE("Git 树", 20, GitTreeSkillReleaseStorageMetadata.class),

    /**
     * 内联：把内容直接写进 storage JSON。只适合调试或极小单文件，装不下附属目录，正式广场不要用。
     */
    INLINE("内联", 30, InlineSkillReleaseStorageMetadata.class),

    ;

    private final String name;

    private final Integer value;

    private final Class<? extends AbstractSkillReleaseStorageMetadata> targetClass;
}
