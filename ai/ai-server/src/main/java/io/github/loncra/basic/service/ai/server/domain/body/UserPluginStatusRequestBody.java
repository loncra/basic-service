package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.OwnershipEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserPluginStatusRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 8241305947621038457L;

    @NotNull
    private PluginTargetTypeEnum targetType;

    @NotNull
    private Long packageId;

    @NotNull
    private OwnershipEnum scope;

    @NotNull
    private PluginInstallStatusEnum status;
}
