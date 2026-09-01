package io.github.loncra.basic.service.ai.server.domain.body;

import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 当前用户的插件安装（含指定工作空间 id）
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPluginInstallResult extends AiUserPluginInstallEntity {

    @Serial
    private static final long serialVersionUID = 7384920165830471926L;

    /**
     * 指定工作空间时的白名单；全部范围为空。
     */
    private List<Long> agentConversationIds = new LinkedList<>();
}
