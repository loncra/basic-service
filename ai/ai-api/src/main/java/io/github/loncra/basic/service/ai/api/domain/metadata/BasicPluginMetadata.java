package io.github.loncra.basic.service.ai.api.domain.metadata;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.resource.api.domain.metadata.DataDictionaryMetadata;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
public class BasicPluginMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = -8672651349574356975L;

    /**
     * 展示名称
     */
    private String name;

    /**
     * 标签
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<String> tags = new LinkedList<>();

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private DataDictionaryMetadata group;

    private String additionalInformation;

}
