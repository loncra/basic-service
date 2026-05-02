package io.github.loncra.basic.service.auth.server.domain.metdata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;
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
public class ResourceMetadata extends IdResourceAuthorityMetadata implements Tree<String, ResourceMetadata> {

    @Serial
    private static final long serialVersionUID = 4709419291009298510L;

    /**
     * 类型
     */
    @NotEmpty
    @Length(max = 16)
    private String type;

    /**
     * 来源
     *
     * @see ResourceSourceEnum
     */
    @NotEmpty
    @Length(max = 16)
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
    private String parentId;

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
    private List<Tree<String, ResourceMetadata>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public String getParent() {
        return parentId;
    }

}