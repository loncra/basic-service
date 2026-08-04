package io.github.loncra.basic.service.ai.server.dao.hub;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_ai_skill_package 的数据访问
 *
 * <p>Table: tb_ai_skill_package - Skill 目录</p>
 *
 * @see AiSkillPackageEntity
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Mapper
@Repository
public interface AiSkillPackageDao extends BaseMapper<AiSkillPackageEntity> {

}
