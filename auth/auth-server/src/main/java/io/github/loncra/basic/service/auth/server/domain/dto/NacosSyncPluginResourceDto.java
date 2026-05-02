package io.github.loncra.basic.service.auth.server.domain.dto;

import io.github.loncra.basic.service.auth.server.service.plugin.disconvery.NacosPluginInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NacosSyncPluginResourceDto extends AbstractSyncPluginResourceDto {

    @Serial
    private static final long serialVersionUID = 5586498341428272800L;

    private NacosPluginInstance instance;

    @Override
    public String getServiceName() {
        return instance.getServiceName();
    }
}
