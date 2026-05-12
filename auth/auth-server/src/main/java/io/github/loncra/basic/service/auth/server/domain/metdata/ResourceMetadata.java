package io.github.loncra.basic.service.auth.server.domain.metdata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.security.entity.ResourceAuthority;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 资源元数据
 *
 * @author maurice
 * @since 2020-04-13 09:48:05
 */
@Data
@NoArgsConstructor
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
    @NotNull
    private ResourceTypeEnum type;

    /**
     * 所属来源
     *
     * @see ResourceSourceEnum
     */
    @NotEmpty
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(ResourceSourceEnum.class)
    private List<ResourceSourceEnum> sources;

    /**
     * 版本号
     */
    @Length(max = 16)
    private String version;

    /**
     * 父类 id
     */
    private Long parentId;

    /**
     * 子节点
     */
    @TableField(exist = false)
    @ToString.Exclude
    private List<Tree<Long, ResourceMetadata>> children = new LinkedList<>();

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ResourceMetadata metadata = CastUtils.cast(o);
        return Objects.equals(getName(), metadata.getName()) && Objects.equals(getAuthority(), metadata.getAuthority()) && Objects.equals(code, metadata.code) && Objects.equals(applicationName, metadata.applicationName) && type == metadata.type && Objects.equals(sources, metadata.sources) && Objects.equals(version, metadata.version) && Objects.equals(parentId, metadata.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAuthority(), code, applicationName, type, sources, version, parentId);
    }


}