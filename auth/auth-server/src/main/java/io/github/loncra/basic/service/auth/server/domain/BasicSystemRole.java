package io.github.loncra.basic.service.auth.server.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.security.entity.RoleAuthority;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.Instant;
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
public class BasicSystemRole extends RoleAuthority implements VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = -8658755200721315766L;

    private Long id;

    @Version
    private Integer version;

    @EqualsAndHashCode.Exclude
    private Instant creationTime;

    /**
     * 是否禁用
     */
    private YesOrNo enabled;

    /**
     * 角色来源
     */
    @JsonCollectionGenericType(ResourceSourceEnum.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<ResourceSourceEnum> sources;

    /**
     * 资源 id 集合
     */
    @JsonCollectionGenericType(Long.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private List<Long> resourceIds = new LinkedList<>();

}
