package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


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

}
