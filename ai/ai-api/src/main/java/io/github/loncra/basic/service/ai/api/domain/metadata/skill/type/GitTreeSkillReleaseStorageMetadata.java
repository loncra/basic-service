package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillReleaseStorageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * Git 某次 commit/tree（审计用；用户安装仍落到对象存储）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GitTreeSkillReleaseStorageMetadata extends AbstractSkillReleaseStorageMetadata {

    @Serial
    private static final long serialVersionUID = 5172836490527183645L;

    private String url;

    private String sha;

    private String path;

    @Override
    public String getType() {
        return SkillReleaseStorageTypeEnum.GIT_TREE.toString();
    }
}
