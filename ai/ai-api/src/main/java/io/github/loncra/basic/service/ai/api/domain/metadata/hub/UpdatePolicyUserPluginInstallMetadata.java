package io.github.loncra.basic.service.ai.api.domain.metadata.hub;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class UpdatePolicyUserPluginInstallMetadata extends AbstractUserPluginInstallMetadata {

    @Serial
    private static final long serialVersionUID = 2476945776271812908L;

    /**
     * 锁定的 {@code tb_ai_skill_release.id}
     */
    private Long releaseId;

    /**
     * 锁定的 semver
     */
    private String releaseVersion;
}
