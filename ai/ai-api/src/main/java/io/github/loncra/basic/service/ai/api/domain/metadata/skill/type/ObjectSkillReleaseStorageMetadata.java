package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillReleaseStorageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 对象存储上的完整目录树
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ObjectSkillReleaseStorageMetadata extends AbstractSkillReleaseStorageMetadata {

    @Serial
    private static final long serialVersionUID = 4061725389416072534L;

    private String prefix;

    private String bucket;

    @Override
    public String getType() {
        return SkillReleaseStorageTypeEnum.OBJECT.toString();
    }
}
