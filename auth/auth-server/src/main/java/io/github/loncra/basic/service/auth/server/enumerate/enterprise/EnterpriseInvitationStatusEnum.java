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
public enum EnterpriseInvitationStatusEnum implements NameValueEnum<Integer> {

    PENDING(10, "待接受"),

    ACCEPTED(20, "已接受"),

    EXPIRED(30, "已过期"),

    CANCELLED(40, "已取消"),

    ;

    private final Integer value;

    private final String name;
}
