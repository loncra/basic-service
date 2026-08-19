package io.github.loncra.basic.service.ai.api.domain.metadata.skill;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Skill 不可变版本的存储（多态）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public abstract class AbstractSkillReleaseStorageMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 6291847305619283740L;

    abstract public String getType();
}
