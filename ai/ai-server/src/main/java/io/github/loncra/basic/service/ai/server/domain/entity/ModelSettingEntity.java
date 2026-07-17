package io.github.loncra.basic.service.ai.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.ai.server.domain.ModelDefinition;
import io.github.loncra.basic.service.ai.server.enumerate.ChatModelEnum;
import io.github.loncra.basic.service.ai.server.enumerate.ImageModelEnum;
import io.github.loncra.basic.service.ai.server.enumerate.ModelTypeEnum;
import io.github.loncra.basic.service.ai.server.enumerate.VideoModelEnum;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;


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
public class ModelSettingEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -1671323411054155019L;

    /**
     * 名称
     */
    private String name;

    /**
     * 封面
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private ObjectWriteResult cover;

    /**
     * 类型
     */
    private ModelTypeEnum type;

    /**
     * 模型值
     */
    private String model;

    /**
     * 厂商
     */
    private Integer manufacturer;

    /**
     * 是否启用
     */
    private YesOrNo enabled = YesOrNo.Yes;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 描述
     */
    private String description;

    public ModelDefinition getManufacturerModelDefinition() {
        if (ModelTypeEnum.CHAT.equals(type)) {
            return ValueEnum.ofEnum(ChatModelEnum.class, manufacturer);
        } else if (ModelTypeEnum.IMAGE.equals(type)) {
            return ValueEnum.ofEnum(ImageModelEnum.class, manufacturer);
        } else if (ModelTypeEnum.VIDEO.equals(type)) {
            return ValueEnum.ofEnum(VideoModelEnum.class, manufacturer);
        }
        return null;
    }
}