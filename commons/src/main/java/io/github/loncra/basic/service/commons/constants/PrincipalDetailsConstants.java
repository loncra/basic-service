package io.github.loncra.basic.service.commons.constants;

import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.security.audit.AuditPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * spring security 用户明细常量，对应 {@link AbstractAuthenticationToken#getDetails()} 的 key
 *
 * @author maurice.chen
 */
public interface PrincipalDetailsConstants {

    /**
     * 是否新用户
     */
    String NEW_USER_KEY = "isNew";

    /**
     * 真实姓名
     */
    String REAL_NAME_KEY = "realName";

    /**
     * 昵称
     */
    String NICKNAME_KEY = "nickname";

    /**
     * 电子邮箱
     */
    String EMAIL_KEY = "email";

    /**
     * 是否校验邮箱
     */
    String EMAIL_VERIFIED_KEY = "emailVerified";

    /**
     * 手机号码
     */
    String PHONE_NUMBER_KEY = "phoneNumber";

    /**
     * 手机号码是否验证码
     */
    String PHONE_NUMBER_VERIFIED_KEY = "phoneNumberVerified";

    /**
     * 性别
     */
    String GENDER_KEY = "gender";

    /**
     * 头像
     */
    String AVATAR_KEY = "avatar";

    /**
     * 备注
     */
    String REMARK_KEY = "remark";

    String GROUP_INFO_KEY = "groupsInfo";

    String USERNAME_KEY = "username";

    String USER_INITIALIZATION_METADATA_KEY = "initialization";

    String PRINCIPAL_KEY = "principal";


    String SYSTEM_NAME_KEY = "systemName";

    static void equals(
            AuditPrincipal source,
            AuditAuthenticationToken target
    ) {
        equals(source, target, "ID 为 [" + target.getSecurityPrincipal()
                .getId() + "] 的用户无法操作不属于自己的数据");
    }

    static void equals(
            AuditPrincipal source,
            AuditAuthenticationToken target,
            String message
    ) {
        contains(List.of(source), target, message);
    }

    static void equals(
            AuditPrincipal source,
            AuditPrincipal target
    ) {
        equals(source, target, "ID 为 [" + target.getPrincipal() + "] 的用户无法操作不属于自己的数据");
    }

    static void equals(
            AuditPrincipal source,
            AuditPrincipal target,
            String message
    ) {
        contains(List.of(source), target, message);
    }

    static void contains(
            List<AuditPrincipal> sources,
            AuditAuthenticationToken target
    ) {
        contains(sources, target, "ID 为 [" + target.getName() + "] 的用户无法操作不属于自己的数据");
    }

    static void contains(
            List<AuditPrincipal> sources,
            AuditAuthenticationToken target,
            String message
    ) {
        SystemException.isTrue(
                sources.stream()
                        .anyMatch(t -> Strings.CS.equals(t.getPrincipal(), target.getName())),
                message
        );
    }

    static void contains(
            List<AuditPrincipal> sources,
            AuditPrincipal target
    ) {
        contains(sources, target, "ID 为 [" + target.getPrincipal() + "] 的用户无法操作不属于自己的数据");
    }

    static void contains(
            List<AuditPrincipal> sources,
            AuditPrincipal target,
            String message
    ) {
        SystemException.isTrue(
                sources.stream()
                        .anyMatch(t -> Strings.CS.equals(t.getPrincipal(), target.getPrincipal())),
                message
        );
    }

    static String getPrincipalName(Map<String, Object> principal) {
        return Objects.toString(principal.get(REAL_NAME_KEY), principal.getOrDefault(NICKNAME_KEY, StringUtils.EMPTY)
                .toString());
    }

    static String getPrincipalName(AuditAuthenticationToken token) {
        AuditAuthenticationSuccessDetails details = CastUtils.cast(token.getDetails());
        return Objects.toString(details.getMetadata()
                                        .get(REAL_NAME_KEY), Objects.toString(details.getMetadata()
                                                                                      .get(NICKNAME_KEY), token.getName()));
    }

}
