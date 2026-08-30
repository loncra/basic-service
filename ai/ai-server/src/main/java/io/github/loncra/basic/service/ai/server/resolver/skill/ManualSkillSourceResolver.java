package io.github.loncra.basic.service.ai.server.resolver.skill;

import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.entity.hub.AiSkillPackageEntity;
import io.github.loncra.basic.service.ai.server.resolver.SkillSourceResolver;
import org.springframework.stereotype.Component;

/**
 * 手工来源：目录由管理端上传，insert 时已将 executeStatus 设为 Success。
 *
 * @author maurice.chen
 */
@Component
public class ManualSkillSourceResolver implements SkillSourceResolver {

    @Override
    public boolean isSupport(SkillSourceTypeEnum sourceType) {
        return SkillSourceTypeEnum.MANUAL.equals(sourceType);
    }

    @Override
    public void ingest(AiSkillPackageEntity entity) {
        // 手工目录由表单上传，无需异步物化
    }
}
