package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>Table: tb_enterprise - 企业表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("enterprise")
@EqualsAndHashCode(callSuper = true)
@TableName("tb_enterprise")
public class EnterpriseEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -4085791497195069697L;

    /**
     * 企业名称
     */
    @NotBlank
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 企业主
     */
    private String ownerPrincipal;

    /**
     * 是否启用
     */
    private YesOrNo enabled = YesOrNo.Yes;

    /**
     * 解散时间
     */
    private Instant disbandTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户 id
     */
    private String tenantId;
}
