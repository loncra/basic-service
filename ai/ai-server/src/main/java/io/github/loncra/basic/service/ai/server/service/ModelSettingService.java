package io.github.loncra.basic.service.ai.server.service;

import io.github.loncra.basic.service.ai.server.dao.ModelSettingDao;
import io.github.loncra.basic.service.ai.server.domain.entity.ModelSettingEntity;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * tb_model_setting 的业务逻辑
 *
 * <p>Table: tb_model_setting - 模型配置</p>
 *
 * @see ModelSettingEntity
 *
 * @author maurice.chen
 *
 * @since 2026-03-30 07:51:00
 */
@Service
@RequiredArgsConstructor
public class ModelSettingService extends BasicService<ModelSettingDao, ModelSettingEntity> {

}
