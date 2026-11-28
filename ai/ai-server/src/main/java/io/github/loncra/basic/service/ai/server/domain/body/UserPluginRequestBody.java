package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginInstallWorkspaceScopeEnum;
import io.github.loncra.basic.service.ai.api.enumerate.hub.PluginTargetTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.basic.service.commons.enumerate.OwnershipEnum;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 安装归属:10.个人,20.企业；全部范围时决定落 principal 行还是 tenant 行，
     * 指定工作空间时须与所选工作空间的归属一致，企业归属仅企业管理员可用
     */
    private OwnershipEnum scope = OwnershipEnum.USER;

    private Map<String, Object> metadata = new LinkedHashMap<>();
}
