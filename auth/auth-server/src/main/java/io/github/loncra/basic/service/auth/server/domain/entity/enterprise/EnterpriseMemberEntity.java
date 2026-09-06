package io.github.loncra.basic.service.auth.server.domain.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.PhoneNumberPrincipal;
import io.github.loncra.basic.service.auth.server.domain.UserInitialization;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.domain.metdata.UserInitializationMetadata;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberInvitationEnum;
import io.github.loncra.basic.service.auth.server.enumerate.enterprise.EnterpriseMemberRoleEnum;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.GenderEnum;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.tenant.TenantEntity;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.io.Serial;
import java.time.Instant;
import java.util.*;

/**
 * <p>Table: tb_enterprise_member - 企业成员表</p>
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@Alias("enterpriseMember")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_enterprise_member", autoResultMap = true)
public class EnterpriseMemberEntity extends AbstractBasicSystemUser implements PhoneNumberPrincipal, UserInitialization, TenantEntity<String> {

    @Serial
    private static final long serialVersionUID = -4587229919034627913L;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version = 0;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

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
     * 企业内部角色 id
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<Long> roleIds = new LinkedHashSet<>();

    /**
     * 企业内部角色 id
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<Long> resourceIds = new LinkedHashSet<>();

    /**
     * 成员角色
     */
    @NotNull
    private EnterpriseMemberRoleEnum role = EnterpriseMemberRoleEnum.MEMBER;

    /**
     * 成员加入状态
     */
    @NotNull
    private EnterpriseMemberInvitationEnum invitation = EnterpriseMemberInvitationEnum.INVITED;

    private Instant lastAuthenticationTime;

    private String tenantId;

    @Getter(AccessLevel.NONE)
    @TableField(exist = false)
    private PersonalUserEntity personalUser;

    @Override
    public Map<String, Object> toPrincipalMetadata() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (Objects.nonNull(personalUser)) {
            result.putAll(personalUser.toPrincipalMetadata());
        }
        result.put(PrincipalDetailsConstants.PRINCIPAL_KEY, principal);
        result.put(TenantEntity.TENANT_ID_FIELD, getTenantId());
        return result;
    }

    @Override
    public ResourceSourceEnum getType() {
        return ResourceSourceEnum.ENTERPRISE;
    }

    @Override
    public String getPhoneNumber() {
        if (Objects.isNull(personalUser)) {
            return null;
        }
        return personalUser.getPhoneNumber();
    }

    @Override
    public YesOrNo getPhoneNumberVerified() {
        if (Objects.isNull(personalUser)) {
            return YesOrNo.No;
        }
        return personalUser.getPhoneNumberVerified();
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        if (Objects.isNull(personalUser)) {
            return ;
        }
        personalUser.setPhoneNumber(phoneNumber);
    }

    @Override
    public void setPhoneNumberVerified(YesOrNo phoneNumberVerified) {
        if (Objects.isNull(personalUser)) {
            return ;
        }
        personalUser.setPhoneNumberVerified(phoneNumberVerified);
    }

    @Override
    public UserInitializationMetadata getInitialization() {
        if (Objects.isNull(personalUser)) {
            return new UserInitializationMetadata();
        }
        return personalUser.getInitialization();
    }

    public GenderEnum getGender() {
        if (Objects.isNull(personalUser)) {
            return GenderEnum.UNKNOWN;
        }
        return personalUser.getGender();
    }

    public String getNickname() {
        if (Objects.isNull(personalUser)) {
            return null;
        }
        return personalUser.getNickname();
    }

}
