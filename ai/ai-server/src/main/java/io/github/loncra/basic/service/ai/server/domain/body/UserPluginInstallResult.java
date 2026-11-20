package io.github.loncra.basic.service.ai.server.domain.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.server.domain.metadata.plugin.UserPluginMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.agent.ExecutionChannelEnum;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前用户的插件安装（含指定工作空间 id）
 *
 * @author maurice.chen
 */
@Data
@JsonIgnoreProperties("packageId")
@EqualsAndHashCode(callSuper = true)
public class UserPluginInstallResult extends UserPluginMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 7384920165830471926L;

    /**
     * 指定工作空间时的白名单；全部范围为空。
     */
    private IdNameValueMetadata<String, ExecutionChannelEnum> workspace;

    private PluginPackageMetadata pluginPackage;
}

