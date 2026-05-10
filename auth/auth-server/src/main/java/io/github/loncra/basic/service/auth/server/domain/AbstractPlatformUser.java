package io.github.loncra.basic.service.auth.server.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.CryptoProperties;
import io.github.loncra.framework.mybatis.plus.annotation.Decryption;
import io.github.loncra.framework.mybatis.plus.annotation.Encryption;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import io.github.loncra.framework.mybatis.plus.baisc.support.IntegerVersionEntity;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.time.Instant;
import java.util.*;

/**
 * 平台用户实体
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractPlatformUser extends AbstractBasicSystemUser implements EmailPrincipal, VersionEntity<Integer, Long> {

    @Serial
    private static final long serialVersionUID = 750742816513263456L;
    /**
     * 密码字段名称
     */
    public static final String PASSWORD_FIELD_NAME = "password";

    /**
     * 最后登陆时间字段名称
     */
    public static final String LAST_AUTHENTICATION_TIME_FIELD_NAME = "lastAuthenticationTime";

    /**
     * 角色字段名称
     */
    public static final String GROUPS_INFO_FIELD_NAME = "roles";

    /**
     * 主键 id
     */
    private Long id;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Instant creationTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version = 0;

    /**
     * 邮箱
     */
    @Email
    @Length(max = 64)
    @JsonSerialize(using = DesensitizeSerializer.class)
    @Decryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    @Encryption(beanName = CryptoProperties.MYBATIS_PLUS_DATA_AES_CRYPTO_SERVICE_NAME)
    private String email;

    /**
     * 是否已验证码邮箱
     */
    private YesOrNo emailVerified = YesOrNo.No;

    /**
     * 拥有角色
     */
    @JsonCollectionGenericType(Long.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<Long> roleIds = new HashSet<>();

    /**
     * 拥有资源
     */
    @JsonCollectionGenericType(Long.class)
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<Long> resourceIds = new HashSet<>();

    /**
     * 密码
     */
    @JsonIgnore
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String password;

    /**
     * 最后认证(登录)时间
     */
    private Instant lastAuthenticationTime;

    /**
     * 转换 security user 明细元数据
     *
     * @return map
     */
    @Override
    public Map<String, Object> toPrincipalMetadata() {

        Map<String, Object> result = createBasicMetadata();

        if (StringUtils.isNotBlank(email)) {
            result.put(PrincipalDetailsConstants.EMAIL_KEY, email);
            result.put(PrincipalDetailsConstants.EMAIL_VERIFIED_KEY, emailVerified);
        }

        if (this instanceof PhoneNumberPrincipal phoneNumberPrincipal) {
            result.put(PrincipalDetailsConstants.PHONE_NUMBER_VERIFIED_KEY, phoneNumberPrincipal.getPhoneNumberVerified());
            result.put(PrincipalDetailsConstants.PHONE_NUMBER_KEY, phoneNumberPrincipal.getPhoneNumber());
        }

        if (this instanceof UserInitialization userInitialization) {
            result.put(PrincipalDetailsConstants.USER_INITIALIZATION_METADATA_KEY, userInitialization.getInitialization());
        }

        return result;
    }

    private Map<String, Object> createBasicMetadata() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (Objects.nonNull(getCreationTime())) {
            result.put(IntegerVersionEntity.CREATION_TIME_FIELD_NAME, getCreationTime());
        }

        return result;
    }

    @Override
    @JsonIgnore
    public String getSystemName() {
        return getType().getValue() + CacheProperties.DEFAULT_SEPARATOR + getId();
    }

}
