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

    EXECUTION(10, "生效中"),

    EXPIRED(20, "已过期"),

    CANCELLED(30, "已取消"),

    ;

    private final Integer value;

    private final String name;
}
