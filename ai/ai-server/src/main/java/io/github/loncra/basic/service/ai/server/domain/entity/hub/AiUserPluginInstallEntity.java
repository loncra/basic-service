package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.ai.server.domain.metadata.plugin.UserPluginMetadata;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


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
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_ai_user_plugin_install", autoResultMap = true)
public class AiUserPluginInstallEntity extends UserPluginMetadata implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -4734179276725670389L;

    private Long id;

    private Instant creationTime = Instant.now();

    @Version
    private Integer version;

    /**
     * 用户唯一识别
     */
    private String principal;

    /**
     * 租户 id
     */
    private String tenantId;

}