package io.github.loncra.basic.service.ai.server.dao.hub;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallSpecificEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_ai_user_plugin_install_specific 的数据访问
 *
 * <p>Table: tb_ai_user_plugin_install_specific - 用户安装广场插件绑定的工作空间</p>
 *
 * @see AiUserPluginInstallSpecificEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Mapper
@Repository
public interface AiUserPluginInstallSpecificDao extends BaseMapper<AiUserPluginInstallSpecificEntity> {

}
