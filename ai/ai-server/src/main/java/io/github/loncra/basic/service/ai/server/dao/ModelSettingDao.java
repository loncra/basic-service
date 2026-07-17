package io.github.loncra.basic.service.ai.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_model_setting 的数据访问
 *
 * <p>Table: tb_model_setting - 模型配置</p>
 *
 * @see ModelSettingEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-30 07:51:00
 */
@Mapper
@Repository
public interface ModelSettingDao extends BaseMapper<ModelSettingEntity> {

}
