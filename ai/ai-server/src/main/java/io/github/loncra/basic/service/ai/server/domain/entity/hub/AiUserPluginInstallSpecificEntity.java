package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_ai_user_plugin_install_specific - 用户安装广场插件绑定的工作空间</p>
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Data
@NoArgsConstructor
@Alias("aiUserPluginInstallSpecific")
@TableName("tb_ai_user_plugin_install_specific")
@EqualsAndHashCode(callSuper = true)
public class AiUserPluginInstallSpecificEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -3198310716815044601L;

    /**
     * tb_ai_user_plugin_install.id
     */
    private Long aiUserPluginInstallId;

    /**
     * 工作空间 tb_agent_conversation.id（DEFAULT/CUSTOMIZE）
     */
    private Long agentConversationId;

}