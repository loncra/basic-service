package io.github.loncra.basic.service.auth.server.domain.metdata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.security.entity.ResourceAuthority;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 资源元数据
 *
 * @author maurice
 * @since 2020-04-13 09:48:05
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceMetadata extends ResourceAuthority implements Tree<Long, ResourceMetadata> {

    @Serial
    private static final long serialVersionUID = 4709419291009298510L;

    private String code;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 类型
     */
    @NotEmpty
    @Length(max = 16)
    private ResourceTypeEnum type;

    /**
     * 所属来源
     *
     * @see ResourceSourceEnum
     */
    @NotEmpty
    @Length(max = 16)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(ResourceSourceEnum.class)
    private List<ResourceSourceEnum> sources;

    /**
     * 版本号
     */
    @NotEmpty
    @Length(max = 16)
    private String version;

    /**
     * 父类 id
     */
    private Long parentId;

    /**
     * 顺序值
     */
    @Range(min = 0, max = 999)
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Long, ResourceMetadata>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }

}