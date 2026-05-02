package io.github.loncra.basic.service.auth.server.domain.dto;

import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractSyncPluginResourceDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 5586498341428272800L;

    private List<ResourceMetadata> resources = new LinkedList<>();

    /**
     * 获取服务名称
     *
     * @return 服务名称
     */
    public abstract String getServiceName();
}
