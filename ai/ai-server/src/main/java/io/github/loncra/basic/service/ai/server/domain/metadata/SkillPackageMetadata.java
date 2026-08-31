package io.github.loncra.basic.service.ai.server.domain.metadata;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Skill 目录 metadata（对标 {@code McpPackageMetadata}）
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class SkillPackageMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 7394058612749305167L;

    private Map<String, Object> source = new LinkedHashMap<>();

    public <T extends AbstractSkillSourceMetadata> T obtainSource() {
        return obtainSource(source);
    }

    public static <T extends AbstractSkillSourceMetadata> T obtainSource(Map<String, Object> source) {
        if (MapUtils.isEmpty(source)) {
            return null;
        }

        Object type = source.get(TypeIdNameMetadata.TYPE_FIELD_NAME);
        if (type instanceof Number number) {
            type = number.intValue();
        }
        SkillSourceTypeEnum sourceTypeEnum = ValueEnum.ofEnum(SkillSourceTypeEnum.class, type);
        return CastUtils.cast(CastUtils.convertValue(source, sourceTypeEnum.getTargetClass()));
    }
}
