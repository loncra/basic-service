package io.github.loncra.basic.service.ai.server.resolver;

import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;

/**
 * Skill 目录来源物化解析器。
 */
public interface SkillSourceResolver {

    /**
     * 是否支持化
     *
     * @param sourceType 来源类型
     *
     * @return
     */
    boolean isSupport(SkillSourceTypeEnum sourceType);

    /**
     * 摄取
     *
     * @param entity skill 实体
     */
    void ingest(AiSkillPackageEntity entity);
}
