package io.github.loncra.basic.service.ai.server.domain.metadata;

import io.github.loncra.basic.service.ai.api.domain.metadata.mcp.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.McpClientTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.metadata.clarify.McpClarifyToolPolicyMetadata;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class McpPackageMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -5408105700742175241L;

    private Map<String, Object> client = new LinkedHashMap<>();

    private List<McpClarifyToolPolicyMetadata> clarifyPolicies = new LinkedList<>();

    public <T extends AbstractMcpClientTransportMetadata> T obtainClientTransport() {
        return obtainClientTransport(client);
    }

    public static <T extends AbstractMcpClientTransportMetadata> T obtainClientTransport(Map<String, Object> client) {
        if (MapUtils.isEmpty(client)) {
            return null;
        }

        String type = client.get(TypeIdNameMetadata.TYPE_FIELD_NAME).toString();
        McpClientTypeEnum clientTypeEnum = NameEnum.ofEnum(McpClientTypeEnum.class, type);
        return CastUtils.cast(CastUtils.convertValue(client, clientTypeEnum.getTargetClass()));
    }
}
