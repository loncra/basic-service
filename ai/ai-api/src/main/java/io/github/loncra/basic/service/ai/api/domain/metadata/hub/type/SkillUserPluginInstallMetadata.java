package io.github.loncra.basic.service.ai.api.domain.metadata.hub.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.UpdatePolicyEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * Skill 安装 metadata：锁定不可变 Release。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SkillUserPluginInstallMetadata extends AbstractUserPluginInstallMetadata {

    @Serial
    private static final long serialVersionUID = 3918572046183749201L;

    /**
     * 锁定的 {@code tb_ai_skill_release.id}
     */
    private Long releaseId;

    /**
     * 锁定的 semver
     */
    private String releaseVersion;

    /**
     * Release 内容指纹
     */
    private String contentHash;

    /**
     * 安装时从包上拷贝的更新策略
     */
    private UpdatePolicyEnum updatePolicy;

    @Override
    public PluginTargetTypeEnum getType() {
        return PluginTargetTypeEnum.SKILL;
    }
}
