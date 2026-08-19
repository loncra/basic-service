package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.type.ArchiveSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.type.GitSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.type.ManagedSkillSourceMetadata;
import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Skill 目录来源类型
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SkillSourceTypeEnum implements NameValueEnum<Integer> {

    GIT("Git", 10, GitSkillSourceMetadata.class),

    ARCHIVE("压缩包", 20, ArchiveSkillSourceMetadata.class),

    MANAGED("广场托管", 30, ManagedSkillSourceMetadata.class),

    ;

    private final String name;

    private final Integer value;

    private final Class<? extends AbstractSkillSourceMetadata> targetClass;
}
