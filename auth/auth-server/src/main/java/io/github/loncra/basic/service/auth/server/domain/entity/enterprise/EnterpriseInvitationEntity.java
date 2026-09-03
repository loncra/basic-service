package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseInvitationStatusEnum;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.mybatis.plus.CryptoProperties;
import io.github.loncra.framework.mybatis.plus.annotation.Decryption;
import io.github.loncra.framework.mybatis.plus.annotation.Encryption;
import io.github.loncra.framework.mybatis.plus.baisc.support.LongVersionEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;

/**
 * <p>Table: tb_enterprise_invitation - 企业邀请表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("enterpriseInvitation")
@EqualsAndHashCode(callSuper = true)
@TableName("tb_enterprise_invitation")
public class EnterpriseInvitationEntity extends LongVersionEntity<Integer> {

    @Serial
    private static final long serialVersionUID = -6319209699259121659L;

    /**
     * 企业 id
     */
    @NotNull
    private Long enterpriseId;

    /**
     * 邀请码
     */
    @NotBlank
    private String code;

    /**
     * 被邀请手机号
     */
    @NotBlank
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION)
    @Decryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    @Encryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    private String phoneNumber;

    /**
     * 邀请人
     */
    @NotBlank
    private String inviterPrincipal;

    /**
     * 邀请状态
     */
    @NotNull
    private EnterpriseInvitationStatusEnum status = EnterpriseInvitationStatusEnum.PENDING;

    /**
     * 过期时间
     */
    @NotNull
    private Instant expirationTime;
}
