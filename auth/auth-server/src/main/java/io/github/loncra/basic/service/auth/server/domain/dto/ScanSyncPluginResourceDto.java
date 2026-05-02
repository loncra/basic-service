package io.github.loncra.basic.service.auth.server.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScanSyncPluginResourceDto extends AbstractSyncPluginResourceDto {
    String serviceName;

    @Override
    public String getServiceName() {
        return serviceName;
    }
}
