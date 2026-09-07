package io.github.loncra.basic.service.auth.server.enumerate.enterprise;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 企业邀请状态
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EnterpriseInvitationAuditEnum implements NameValueEnum<Integer> {

    MANUAL(10, "人工审核"),

    AUTOMATIC(20, "自动通过"),

    ;

    private final Integer value;

    private final String name;
}
