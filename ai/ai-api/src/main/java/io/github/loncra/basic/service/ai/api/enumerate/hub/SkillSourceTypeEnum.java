package io.github.loncra.basic.service.ai.api.enumerate.hub;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.type.GitSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.type.ManualSkillSourceMetadata;
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

    MANUAL("手工录入", 20, ManualSkillSourceMetadata.class),

    ;

    private final String name;

    private final Integer value;

    private final Class<? extends AbstractSkillSourceMetadata> targetClass;
}
