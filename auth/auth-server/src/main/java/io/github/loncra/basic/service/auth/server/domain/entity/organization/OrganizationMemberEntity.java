package io.github.loncra.basic.service.auth.server.domain.entity.organization;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.enumerate.organization.OrganizationMemberStatusEnum;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;

/**
 * <p>Table: tb_organization_member - 企业成员表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("organizationMember")
@EqualsAndHashCode(callSuper = true)
@TableName("tb_organization_member")
public class OrganizationMemberEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -4587229919034627913L;

    /**
     * 企业 id，同时作为企业空间租户 id
     */
    @NotNull
    private Long organizationId;

    /**
     * 成员认证主体
     */
    @NotBlank
    private String principal;

    /**
     * 成员角色
     */
    @NotNull
    private OrganizationMemberRoleEnum role = OrganizationMemberRoleEnum.MEMBER;

    /**
     * 成员状态
     */
    @NotNull
    private OrganizationMemberStatusEnum status = OrganizationMemberStatusEnum.INVITED;
}
