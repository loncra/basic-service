package io.github.loncra.basic.service.auth.server.domain.metdata;

import io.github.loncra.basic.service.auth.server.service.resource.plugin.disconvery.NacosPluginInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NacosSyncPluginResourceMetadata extends SyncPluginResourceMetadata {

    private NacosPluginInstance instance;
}
