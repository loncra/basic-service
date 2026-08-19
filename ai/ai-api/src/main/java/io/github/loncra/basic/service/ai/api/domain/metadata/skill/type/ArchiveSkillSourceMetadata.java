package io.github.loncra.basic.service.ai.api.domain.metadata.skill.type;

import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillSourceMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 压缩包来源：远端 zip 或已上传附件
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArchiveSkillSourceMetadata extends AbstractSkillSourceMetadata {

    @Serial
    private static final long serialVersionUID = 2849503167294850312L;

    private String url;

    private String attachmentId;

    private String sha256;

    @Override
    public String getType() {
        return SkillSourceTypeEnum.ARCHIVE.toString();
    }
}
