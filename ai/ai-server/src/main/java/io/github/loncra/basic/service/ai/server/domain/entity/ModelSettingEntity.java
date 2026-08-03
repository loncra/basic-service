package io.github.loncra.basic.service.ai.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.api.domain.metadata.ModelSettingMetadata;
import io.github.loncra.basic.service.ai.server.enumerate.ModelTypeEnum;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;


/**
 * <p>Table: tb_model_setting - 模型配置</p>
 *
 * @author maurice.chen
 *
 * @since 2026-03-30 07:51:00
 */
@Data
@NoArgsConstructor
@Alias("aiModelSetting")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_ai_model_setting", autoResultMap = true)
public class ModelSettingEntity extends ModelSettingMetadata implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -1671323411054155019L;

    private Integer version;

    private Instant creationTime = Instant.now();

    /**
     * 图标
     */
    private String icon;

    /**
     * 类型
     */
    private ModelTypeEnum type;

    /**
     * 厂商
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private DataDictionaryMetadata manufacturer;

    /**
     * 是否启用
     */
    private YesOrNo enabled = YesOrNo.Yes;

    /**
     * 备注
     */
    private String remark;

    /**
     * 顺序值
     */
    private Integer sort;

    /**
     * 描述
     */
    private String description;
}