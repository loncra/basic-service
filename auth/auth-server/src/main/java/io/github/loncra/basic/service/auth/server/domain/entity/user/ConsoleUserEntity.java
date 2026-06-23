package io.github.loncra.basic.service.auth.server.domain.entity.user;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.CryptoProperties;
import io.github.loncra.framework.mybatis.plus.annotation.Decryption;
import io.github.loncra.framework.mybatis.plus.annotation.Encryption;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

/**
 * <p>系统用户实体类</p>
 * <p>Table: tb_console_user - 系统用户表</p>
 *
 * @author maurice
 * @since 2020-04-13 10:14:46
 */
@Data
@NoArgsConstructor
@Alias("consoleUser")
@Description("员工管理")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_console_user", autoResultMap = true)
public class ConsoleUserEntity extends AbstractPlatformUser implements PhoneNumberPrincipal, UserInitialization {

    @Serial
    private static final long serialVersionUID = 1815468583503444307L;

    /**
     * 真实姓名
     */
    @NotEmpty
    @Length(max = 16)
    @Description(value = "真实姓名", sort = 6)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String realName;

    /**
     * 性别
     */
    @Description(value = "性别", sort = 5)
    private GenderEnum gender = GenderEnum.UNKNOWN;

    /**
     * 联系电话
     */
    @Length(max = 32)
    @Description(value = "联系电话", sort = 7)
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION)
    @Decryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    @Encryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    private String phoneNumber;

    /**
     * 是否已验证码联系电话
     */
    private YesOrNo phoneNumberVerified = YesOrNo.No;

    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private UserInitializationMetadata initialization = new UserInitializationMetadata();

    /**
     * 备注
     */
    @Description(value = "备注", sort = 8)
    private String remark;

    @Override
    public Map<String, Object> toPrincipalMetadata() {

        Map<String, Object> result = super.toPrincipalMetadata();

        if (Objects.nonNull(gender)) {
            result.put(PrincipalDetailsConstants.GENDER_KEY, CastUtils.convertValue(gender, Map.class));
        }

        if (StringUtils.isNotBlank(realName)) {
            result.put(PrincipalDetailsConstants.REAL_NAME_KEY, realName);
        }

        if (StringUtils.isNotBlank(remark)) {
            result.put(PrincipalDetailsConstants.REMARK_KEY, remark);
        }

        return result;
    }

    @Override
    @JsonIgnore
    public ResourceSourceEnum getType() {
        return ResourceSourceEnum.CONSOLE;
    }
}