package io.github.loncra.basic.service.resource.api.domain.metadata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.commons.enumerate.ValueTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * 数据字典元数据
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class DataDictionaryMetadata implements Tree<Long, DataDictionaryMetadata>, Serializable {

    public static final String CODE_FIELD = "code";

    public static final String LEVEL_FIELD = "level";

    @Serial
    private static final long serialVersionUID = -6880817354929730676L;

    /**
     * 根节点为 null
     */
    private Long parentId;

    /**
     * 子类节点
     */
    @TableField(exist = false)
    private List<Tree<Long, DataDictionaryMetadata>> children = new LinkedList<>();

    /**
     * 元数据信息
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Map<String, Object> metadata = new LinkedHashMap<>();

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private Object value;

    /**
     * 键名称
     */
    private String code;

    /**
     * 值类型
     */
    private ValueTypeEnum valueType;

    /**
     * 等级
     */
    private String level;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataDictionaryMetadata that = CastUtils.cast(o);
        return Objects.equals(value, that.value) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, code);
    }

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }
}
