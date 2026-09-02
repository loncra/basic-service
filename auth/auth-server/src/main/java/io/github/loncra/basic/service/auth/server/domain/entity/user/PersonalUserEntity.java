package io.github.loncra.basic.service.auth.server.domain.entity.user;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.PhoneNumberPrincipal;
import io.github.loncra.basic.service.auth.server.domain.UserInitialization;
import io.github.loncra.basic.service.auth.server.domain.metdata.UserInitializationMetadata;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.GenderEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.annotation.Description;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.CryptoProperties;
import io.github.loncra.framework.mybatis.plus.annotation.Decryption;
import io.github.loncra.framework.mybatis.plus.annotation.Encryption;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;


/**
 * <p>Table: tb_personal_user - 个人用户表</p>
 *
 * @author maurice.chen
 *
 * @since 2026-03-28 09:46:07
 */
@Data
@NoArgsConstructor
@Alias("personalUser")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_personal_user", autoResultMap = true)
public class PersonalUserEntity extends AbstractPlatformUser implements PhoneNumberPrincipal, UserInitialization {

    @Serial
    private static final long serialVersionUID = 3436650151058536328L;

    @Description(value = "真实姓名", sort = 6)
    private String nickname;

    @Description(value = "性别", sort = 5)
    private GenderEnum gender = GenderEnum.UNKNOWN;

    /**
     * 电话号码
     */
    @Description(value = "联系电话", sort = 7)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION)
    @Decryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    @Encryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    private String phoneNumber;

    /**
     * 是验证码手机号码
     */
    private YesOrNo phoneNumberVerified = YesOrNo.No;

    /**
     * 个人空间租户 id
     */
    private String tenantId;

    /**
     * 上次使用的企业 id，为空表示个人空间
     */
    private Long lastActiveOrganizationId;

    /**
     * 推荐码
     */
    private String promoCode;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private UserInitializationMetadata initialization = new UserInitializationMetadata();

    @Override
    public ResourceSourceEnum getType() {
        return ResourceSourceEnum.PERSONAL;
    }

    @Override
    public Map<String, Object> toPrincipalMetadata() {

        Map<String, Object> result = super.toPrincipalMetadata();

        if (Objects.nonNull(gender)) {
            result.put(PrincipalDetailsConstants.GENDER_KEY, CastUtils.convertValue(gender, Map.class));
        }
        if (StringUtils.isNotEmpty(getTenantId())) {
            result.put(TenantEntity.TENANT_ID_FIELD, getTenantId());
        }
        return result;
    }
}