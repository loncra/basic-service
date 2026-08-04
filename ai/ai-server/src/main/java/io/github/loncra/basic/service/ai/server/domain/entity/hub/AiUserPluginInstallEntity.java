package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PluginInstallStatusEnum;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PluginInstallUserScopeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Map;


/**
 * <p>Table: tb_ai_user_plugin_install - 用户广场插件统一安装关联</p>
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Data
@NoArgsConstructor
@Alias("aiUserPluginInstall")
@TableName("tb_ai_user_plugin_install")
@EqualsAndHashCode(callSuper = true)
public class AiUserPluginInstallEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -4734179276725670389L;

    /**
     * 目标类型:10.skill,20.mcp
     */
    private PluginTargetTypeEnum targetType;

    /**
     * tb_ai_skill_package.id 或 tb_ai_mcp_package.id，由 target_type 解释
     */
    private Long packageId;

    /**
     * 作用返回 10.用户,20.机构
     */
    private PluginInstallUserScopeEnum scope;

    /**
     * 用户唯一是被
     */
    private String principal;

    /**
     * 租户 id
     */
    private String tenantId;

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

}