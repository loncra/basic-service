package io.github.loncra.basic.service.auth.server.domain.entity.merchant;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;

/**
 * <p>Table: tb_open_platform_merchant - 开放平台商户表</p>
 *
 * @author maurice.chen
 * @since 2023-09-11 08:57:11
 */
@Data
@NoArgsConstructor
@Alias("merchant")
@TableName("tb_open_platform_merchant")
@EqualsAndHashCode(callSuper = true)
public class OpenPlatformMerchantEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = 2170545239707350962L;

    /**
     * 名称
     */
    @NotNull
    private String name;

    /**
     * app id
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String appId;

    /**
     * app key
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String appKey;

    /**
     * 私有密钥
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String privateKey;

    /**
     * 共有密钥
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String publicKey;

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