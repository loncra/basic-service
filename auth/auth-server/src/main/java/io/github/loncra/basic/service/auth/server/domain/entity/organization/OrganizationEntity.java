package io.github.loncra.basic.service.auth.server.domain.entity.organization;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;

/**
 * <p>Table: tb_organization - 企业表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("organization")
@EqualsAndHashCode(callSuper = true)
@TableName("tb_organization")
public class OrganizationEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -4085791497195069697L;

    /**
     * 企业名称
     */
    @NotBlank
    private String name;

    /**
     * 企业主
     */
    @NotBlank
    private String ownerPrincipal;

    /**
     * 是否启用
     */
    @NotNull
    private YesOrNo enabled = YesOrNo.Yes;

    /**
     * 备注
     */
    private String remark;
}
