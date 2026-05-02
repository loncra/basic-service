package io.github.loncra.basic.service.auth.server.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.auth.server.domain.BasicSystemRole;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.tree.Tree;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>用户角色实体类</p>
 * <p>Table: tb_role - 用户角色表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@Alias("role")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_role", autoResultMap = true)
public class RoleEntity extends BasicSystemRole implements Tree<Long, RoleEntity> {

    @Serial
    private static final long serialVersionUID = 5357157352791368716L;

    /**
     * 父类 id
     */
    private Long parentId;

    /**
     * 是否可删除:0.否、1.是
     */
    @NotNull
    private YesOrNo removable;

    /**
     * 是否可修改:0.否、1.是
     */
    @NotNull
    private YesOrNo modifiable;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Long, RoleEntity>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }

}