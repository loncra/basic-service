package io.github.loncra.basic.service.ai.api.domain.metadata.hub.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * MCP 安装 metadata：认证模式与凭据。
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class McpUserPluginInstallMetadata extends AbstractUserPluginInstallMetadata {

    @Serial
    private static final long serialVersionUID = 4829683157294850312L;

    /**
     * 认证模式取值，对应服务端 {@code McpPackageAuthModeEnum} 的 value。
     */
    private Integer authMode;

    /**
     * 用户填写的 API Key，或 OAuth 完成后得到的凭据。本轮安装可空，后续写入。
     */
    private String key;

    @Override
    public PluginTargetTypeEnum getType() {
        return PluginTargetTypeEnum.MCP;
    }
}
