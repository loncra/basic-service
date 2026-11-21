package io.github.loncra.basic.service.ai.api.domain.metadata.hub;

import io.github.loncra.basic.service.commons.enumerate.UpdatePolicyEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdatePolicyPluginPackageMetadata extends PluginPackageMetadata {
    @Serial
    private static final long serialVersionUID = -5993104494987765254L;

    /**
     * 最新已发布 semver
     */
    private String latestVersion;

    /**
     * 更新策略:10.手动更新,20.自动更新
     */
    private UpdatePolicyEnum defaultUpdatePolicy;
}
