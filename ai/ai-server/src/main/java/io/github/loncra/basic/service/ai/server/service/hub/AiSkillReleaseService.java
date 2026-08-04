package io.github.loncra.basic.service.ai.server.service.hub;

import io.github.loncra.basic.service.ai.server.dao.hub.AiSkillReleaseDao;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * tb_ai_skill_release 的业务逻辑
 *
 * <p>Table: tb_ai_skill_release - Skill 不可变版本</p>
 *
 * @see AiSkillReleaseEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Service
@RequiredArgsConstructor
public class AiSkillReleaseService extends BasicService<AiSkillReleaseDao, AiSkillReleaseEntity> {

}
