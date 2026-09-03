package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberStatusEnum;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>Table: tb_enterprise_member - 企业成员表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("enterpriseMember")
@EqualsAndHashCode(callSuper = true)
@TableName("tb_enterprise_member")
public class EnterpriseMemberEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -4587229919034627913L;

    /**
     * 企业 id，同时作为企业空间租户 id
     */
    @NotNull
    private Long enterpriseId;

    /**
     * 成员认证主体
     */
    @NotBlank
    private String principal;

    /**
     * 成员角色
     */
    @NotNull
    private EnterpriseMemberRoleEnum role = EnterpriseMemberRoleEnum.MEMBER;

    /**
     * 成员状态
     */
    @NotNull
    private EnterpriseMemberStatusEnum status = EnterpriseMemberStatusEnum.INVITED;

    private Instant lastAuthenticationTime;
}
