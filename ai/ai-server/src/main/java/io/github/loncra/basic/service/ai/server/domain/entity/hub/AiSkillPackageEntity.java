package io.github.loncra.basic.service.ai.server.domain.entity.hub;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.api.domain.metadata.hub.PluginPackageMetadata;
import io.github.loncra.basic.service.ai.api.enumerate.hub.SkillSourceTypeEnum;
import io.github.loncra.basic.service.ai.server.domain.metadata.SkillPackageMetadata;
import io.github.loncra.basic.service.commons.enumerate.UpdatePolicyEnum;
import io.github.loncra.framework.commons.CastUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;


/**
 * <p>Table: tb_ai_skill_package - Skill 目录</p>
 *
 * @author maurice.chen
 *
 * @since 2026-08-04 09:21:08
 */
@Data
@NoArgsConstructor
@Alias("aiSkillPackage")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_ai_skill_package", autoResultMap = true)
public class AiSkillPackageEntity extends PluginPackageMetadata {

    @Serial
    private static final long serialVersionUID = 7927470412713379855L;

    public static final String SOURCE_FIELD = "source";

    /**
     * 最新已发布 semver
     */
    private String latestVersion;

    /**
     * 更新策略:10.手动更新,20.自动更新
     */
    private UpdatePolicyEnum defaultUpdatePolicy;

    /**
     * 来源类型:10.Git,20.压缩包,30.广场托管
     */
    private SkillSourceTypeEnum sourceType;

    public SkillPackageMetadata obtainMetadata() {
        return CastUtils.convertValue(getMetadata(), SkillPackageMetadata.class);
    }

}