package io.github.loncra.basic.service.ai.server.dao.hub;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_ai_user_plugin_install 的数据访问
 *
 * <p>Table: tb_ai_user_plugin_install - 用户广场插件统一安装关联</p>
 *
 * @see AiUserPluginInstallEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Mapper
@Repository
public interface AiUserPluginInstallDao extends BaseMapper<AiUserPluginInstallEntity> {

}
