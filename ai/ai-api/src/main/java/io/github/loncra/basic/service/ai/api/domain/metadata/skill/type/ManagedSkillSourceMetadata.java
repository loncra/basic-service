package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 广场托管目录（对象存储前缀）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ManagedSkillSourceMetadata extends AbstractSkillSourceMetadata {

    @Serial
    private static final long serialVersionUID = 3950614278305961423L;

    private String prefix;

    @Override
    public String getType() {
        return SkillSourceTypeEnum.MANAGED.toString();
    }
}
