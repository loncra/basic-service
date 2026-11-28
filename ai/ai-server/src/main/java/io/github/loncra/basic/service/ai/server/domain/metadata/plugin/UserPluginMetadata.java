package io.github.loncra.basic.service.ai.server.domain.metadata.plugin;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.AbstractUserPluginInstallMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.OwnershipEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
public class UserPluginMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 3755805938721417416L;

    /**
     * tb_ai_skill_package.id 或 tb_ai_mcp_package.id，由 target_type 解释
     */
    private Long packageId;

    /**
     * 目标类型:10.skill,20.mcp
     */
    private PluginTargetTypeEnum targetType;

    /**
     * 作用范围:10.个人,20.企业；企业行按 tenantId 对全体企业成员生效
     */
    private OwnershipEnum scope;

    /**
     * 影响范围:10.所有工作空间, 20.指定工作空间
     */
    private PluginInstallWorkspaceScopeEnum workspaceScope;

    /**
     * 状态:10.待授权,20.已激活,30.已禁用
     */
    private PluginInstallStatusEnum status;

    /**
     * 扩展: OAuth 密文令牌、非密配置、operator_principal、伴生 hash 等
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata;

    private String exception;

    public <T extends AbstractUserPluginInstallMetadata> T obtainMetadata() {
        if (Objects.isNull(targetType) || Objects.isNull(metadata) || metadata.isEmpty()) {
            return null;
        }
        return CastUtils.cast(CastUtils.convertValue(metadata, targetType.getTargetClass()));
    }

    public static String toUniqueIdentification(UserPluginMetadata pluginMetadata) {
        return toUniqueIdentification(pluginMetadata.getPackageId(),pluginMetadata.getTargetType());
    }

    public static String toUniqueIdentification(Long packageId, PluginTargetTypeEnum targetType) {
        return targetType.getValue() + CacheProperties.DEFAULT_SEPARATOR + packageId;
    }

}
