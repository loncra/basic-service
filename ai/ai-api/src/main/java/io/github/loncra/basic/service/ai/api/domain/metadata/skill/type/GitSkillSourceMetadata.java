package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * Git 仓库来源
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GitSkillSourceMetadata extends AbstractSkillSourceMetadata {

    @Serial
    private static final long serialVersionUID = 1738492056183749201L;

    private String url;

    private String ref;

    private String sha;

    /**
     * 仓库内子目录，可空
     */
    private String path;

    @Override
    public String getType() {
        return SkillSourceTypeEnum.GIT.toString();
    }
}
