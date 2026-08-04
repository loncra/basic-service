package io.github.loncra.basic.service.ai.server.dao.hub;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillReleaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_ai_skill_release 的数据访问
 *
 * <p>Table: tb_ai_skill_release - Skill 不可变版本</p>
 *
 * @see AiSkillReleaseEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Mapper
@Repository
public interface AiSkillReleaseDao extends BaseMapper<AiSkillReleaseEntity> {

}
