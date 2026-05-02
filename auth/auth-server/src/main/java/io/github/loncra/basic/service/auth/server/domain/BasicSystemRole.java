package io.github.loncra.basic.service.auth.server.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdResourceAuthorityMetadata;
import io.github.loncra.framework.spring.security.core.plugin.metadata.IdRoleAuthorityMetadata;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.LinkedList;
import java.util.List;

/**
 * 基础系统角色
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BasicSystemRole extends IdRoleAuthorityMetadata {

    @Serial
    private static final long serialVersionUID = -8658755200721315766L;

    /**
     * 角色来源
     */
    @JsonCollectionGenericType(ResourceSourceEnum.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ResourceSourceEnum> sources;

    /**
     * 资源 id 集合
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(IdResourceAuthorityMetadata.class)
    private List<IdResourceAuthorityMetadata> resources = new LinkedList<>();

}
