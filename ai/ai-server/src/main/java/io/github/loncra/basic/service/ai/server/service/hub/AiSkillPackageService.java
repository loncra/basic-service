package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillPackageDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;

import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

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

}
