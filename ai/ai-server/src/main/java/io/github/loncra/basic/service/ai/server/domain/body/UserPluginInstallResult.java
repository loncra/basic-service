package io.github.loncra.basic.service.ai.server.domain.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallLayerEnum;
import io.github.loncra.basic.service.ai.server.domain.metadata.plugin.UserPluginMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonIgnoreProperties("packageId")
@EqualsAndHashCode(callSuper = true)
public class UserPluginInstallResult extends UserPluginMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -6153209482751034827L;

    private PluginInstallLayerEnum layer;

    private IdNameValueMetadata<String, ExecutionChannelEnum> workspace;

    @JsonIgnoreProperties({"origin","status", "type", "category", "metadata"})
    private PluginPackageMetadata pluginPackage;
}
