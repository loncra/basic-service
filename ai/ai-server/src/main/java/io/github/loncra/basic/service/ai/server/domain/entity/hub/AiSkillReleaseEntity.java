package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.api.domain.metadata.skill.AbstractSkillReleaseStorageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillReleaseStorageTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * <p>Table: tb_ai_skill_release - Skill 不可变版本</p>
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Data
@NoArgsConstructor
@Alias("aiSkillRelease")
@TableName(value = "tb_ai_skill_release", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class AiSkillReleaseEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 3398025201348596465L;

    /**
     * Skill 目录 id
     */
    private Long aiSkillPackageId;

    /**
     * semver，安装锁定对象
     */
    private String releaseVersion;

    /**
     * 内容指纹
     */
    private String contentHash;

    /**
     * schema 多态: 对象存储目录树 / Git tree / inline
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> storage = new LinkedHashMap<>();

    /**
     * 变更说明
     */
    private String changelog;

    /**
     * 发布时间
     */
    private Instant releaseTime;

    /**
     * 是否启用:0.否，1.是
     */
    private YesOrNo enabled;

    public <T extends AbstractSkillReleaseStorageMetadata> T obtainStorage() {
        if (MapUtils.isEmpty(storage)) {
            return null;
        }
        String type = storage.get(TypeIdNameMetadata.TYPE_FIELD_NAME).toString();
        SkillReleaseStorageTypeEnum storageTypeEnum = NameEnum.ofEnum(SkillReleaseStorageTypeEnum.class, type);
        return CastUtils.cast(CastUtils.convertValue(storage, storageTypeEnum.getTargetClass()));
    }

}