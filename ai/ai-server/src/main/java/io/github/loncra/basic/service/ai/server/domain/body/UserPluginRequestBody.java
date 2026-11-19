package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallUserScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户安装或卸载广场插件范围
 *
 * @author maurice.chen
 */
@Data
public class UserPluginRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 6192837465019283746L;

    @NotNull
    private PluginTargetTypeEnum targetType;

    @NotNull
    private Long packageId;

    @NotNull
    private PluginInstallWorkspaceScopeEnum workspaceScope;

    /**
     * 指定工作空间时必填；全部范围忽略。
     */
    private List<IdValueMetadata<String, ExecutionChannelEnum>> workspaces;

    private PluginInstallUserScopeEnum scope = PluginInstallUserScopeEnum.USER;
}
