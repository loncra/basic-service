package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.server.dao.hub.AiUserPluginInstallSpecificDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiUserPluginInstallSpecificEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 *
 * tb_ai_user_plugin_install_specific 的业务逻辑
 *
 * <p>Table: tb_ai_user_plugin_install_specific - 用户安装广场插件绑定的工作空间</p>
 *
 * @see AiUserPluginInstallSpecificEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiUserPluginInstallSpecificService extends BasicService<AiUserPluginInstallSpecificDao, AiUserPluginInstallSpecificEntity> {

    public List<AiUserPluginInstallSpecificEntity> findByInstallId(Long installId) {
        return lambdaQuery()
                .eq(AiUserPluginInstallSpecificEntity::getAiUserPluginInstallId, installId)
                .list();
    }

    public List<AiUserPluginInstallSpecificEntity> findByInstallIds(Collection<Long> installIds) {
        if (CollectionUtils.isEmpty(installIds)) {
            return List.of();
        }
        return lambdaQuery()
                .in(AiUserPluginInstallSpecificEntity::getAiUserPluginInstallId, installIds)
                .list();
    }

}
