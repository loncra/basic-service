package io.github.loncra.basic.service.ai.api.domain.metadata.skill;

import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Skill 目录来源（多态，对标 MCP client transport）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public abstract class AbstractSkillSourceMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 4182736501928475610L;

    abstract public SkillSourceTypeEnum getType();
}
