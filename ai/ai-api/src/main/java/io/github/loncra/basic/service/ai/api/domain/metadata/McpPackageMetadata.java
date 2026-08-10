package io.github.loncra.basic.service.ai.api.domain.metadata;

import io.github.loncra.basic.service.ai.api.domain.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class McpPackageMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -5408105700742175241L;

    private Map<String, Object> client = new LinkedHashMap<>();

    public <T extends AbstractMcpClientTransportMetadata> T obtainClientTransport() {
        if (MapUtils.isEmpty(client)) {
            return null;
        }

        String type = client.get(TypeIdNameMetadata.TYPE_FIELD_NAME).toString();
        McpClientTypeEnum clientTypeEnum = NameEnum.ofEnum(McpClientTypeEnum.class, type);
        return CastUtils.cast(CastUtils.convertValue(client, clientTypeEnum.getTargetClass()));
    }
}
