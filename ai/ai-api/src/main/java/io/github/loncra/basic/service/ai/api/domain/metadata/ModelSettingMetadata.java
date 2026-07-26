package io.github.loncra.basic.service.ai.api.domain.metadata;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelSettingMetadata extends IdEntity<Long> {

    @Serial
    private static final long serialVersionUID = 7378643556142960432L;

    public static final String MODEL_DEFAULT_OPTIONS_KEY = "options";

    /**
     * 模型名称
     */
    private String model;

    /**
     * 名称
     */
    private String name;

    /**
     * 厂商
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private DataDictionaryMetadata manufacturer;

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();
}
