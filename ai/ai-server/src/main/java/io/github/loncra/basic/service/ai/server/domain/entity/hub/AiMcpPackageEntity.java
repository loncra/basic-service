package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.server.domain.metadata.AbstractMcpClientTransportMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.McpClientTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.McpPackageAuthModeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Map;


/**
 * <p>Table: tb_ai_mcp_package - MCP 连接器目录</p>
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Data
@NoArgsConstructor
@Alias("aiMcpPackage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_ai_mcp_package", autoResultMap = true)
public class AiMcpPackageEntity extends PluginPackageMetadata {

    @Serial
    private static final long serialVersionUID = -2090744811869914214L;

    public static final String CLIENT_FIELD = "client";

    /**
     * 认证模式: 10. OAuth, 20. apiKey
     */
    private McpPackageAuthModeEnum authMode;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private TimeProperties initializeTimeout;

    /**
     * 是否动态激活：0.建连直接激活 tools; 1.伴生 Skill+按需激活
     */
    private YesOrNo dynamicActivation;

    public <T extends AbstractMcpClientTransportMetadata> T obtainMcpClientTransport() {
        if (MapUtils.isEmpty(getMetadata()) || !getMetadata().containsKey(CLIENT_FIELD)) {
            return null;
        }
        Map<String, Object> client = CastUtils.convertValue(getMetadata().get(CLIENT_FIELD), CastUtils.MAP_TYPE_REFERENCE);
        if (MapUtils.isEmpty(client)) {
            return null;
        }
        String type = client.get(TypeIdNameMetadata.TYPE_FIELD_NAME).toString();
        McpClientTypeEnum clientTypeEnum = NameEnum.ofEnum(McpClientTypeEnum.class, type);
        return CastUtils.cast(CastUtils.convertValue(client, clientTypeEnum.getTargetClass()));
    }

}