package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.auth.server.domain.BasicSystemRole;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.commons.tree.Tree;
import io.github.loncra.framework.security.entity.RoleAuthority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Table: tb_enterprise_role - 企业用户组表</p>
 *
 * @author maurice.chen
 *
 * @since 2026-09-05 06:30:36
 */
@Data
@NoArgsConstructor
@Alias("enterpriseRole")
@TableName("tb_enterprise_role")
@EqualsAndHashCode(callSuper = true)
public class EnterpriseRoleEntity extends BasicSystemRole implements TenantEntity<String>, Tree<Long, EnterpriseRoleEntity> {

    public static final String DEFAULT_ROLE_PREFIX = "ROLE_ENTERPRISE";

    @Serial
    private static final long serialVersionUID = 5893845610542466933L;

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
     * 租户 id
     */
    private String tenantId;

    /**
     * 子节点
     */
    @TableField(exist = false)
    private List<Tree<Long, EnterpriseRoleEntity>> children = new ArrayList<>();

    @Override
    @JsonIgnore
    public Long getParent() {
        return parentId;
    }

}