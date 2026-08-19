package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillReleaseStorageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 内联存储（仅调试，正式广场不用）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InlineSkillReleaseStorageMetadata extends AbstractSkillReleaseStorageMetadata {

    @Serial
    private static final long serialVersionUID = 6283947501638294756L;

    @Override
    public String getType() {
        return SkillReleaseStorageTypeEnum.INLINE.toString();
    }
}
