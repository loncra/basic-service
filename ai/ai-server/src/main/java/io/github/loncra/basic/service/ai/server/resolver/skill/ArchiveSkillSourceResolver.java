package io.github.loncra.basic.service.ai.server.resolver.skill;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.basic.service.ai.server.resolver.SkillSourceResolver;
import io.github.loncra.framework.commons.exception.SystemException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ArchiveSkillSourceResolver implements SkillSourceResolver {

    @Override
    public boolean isSupport(String type) {
        return SkillSourceTypeEnum.ARCHIVE.toString().equals(type);
    }

    @Override
    public Path materialize(String packageKey, AbstractSkillSourceMetadata metadata) {
        throw new SystemException("ARCHIVE 来源物化尚未实现: " + packageKey);
    }
}
