package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.server.dao.hub.AiUserPluginInstallDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallEntity;

import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 *
 * tb_ai_user_plugin_install 的业务逻辑
 *
 * <p>Table: tb_ai_user_plugin_install - 用户广场插件统一安装关联</p>
 *
 * @see AiUserPluginInstallEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiUserPluginInstallService extends BasicService<AiUserPluginInstallDao, AiUserPluginInstallEntity> {

}
