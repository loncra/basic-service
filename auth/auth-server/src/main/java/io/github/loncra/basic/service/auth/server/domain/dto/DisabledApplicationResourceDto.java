package io.github.loncra.basic.service.auth.server.domain.dto;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import io.github.loncra.basic.service.auth.server.domain.metdata.ResourceMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 禁用应用资源 dto
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class DisabledApplicationResourceDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -6894496043422834494L;

    private NamingEvent event;
    private List<ResourceMetadata> resources;
}
