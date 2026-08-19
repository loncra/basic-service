package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillPackageDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;

import io.github.loncra.basic.service.commons.enumerate.DataStatusEnum;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * tb_ai_skill_package 的业务逻辑
 *
 * <p>Table: tb_ai_skill_package - Skill 目录</p>
 *
 * @see AiSkillPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiSkillPackageService extends BasicService<AiSkillPackageDao, AiSkillPackageEntity> {

    @Transactional(rollbackFor = Exception.class)
    public void release(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(PluginPackageMetadata::getStatus, DataStatusEnum.RELEASE.getValue())
                .eq(PluginPackageMetadata::getId,id)
                .update()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(List<Long> ids) {
        ids.forEach(id -> lambdaUpdate().set(PluginPackageMetadata::getStatus, DataStatusEnum.REVOKE.getValue())
                .eq(PluginPackageMetadata::getId,id)
                .update()
        );
    }
}
