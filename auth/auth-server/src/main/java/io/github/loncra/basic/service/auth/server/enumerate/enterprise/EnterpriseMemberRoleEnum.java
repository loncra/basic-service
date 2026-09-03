package io.github.loncra.basic.service.auth.server.enumerate.enterprise;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 企业成员角色
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EnterpriseMemberRoleEnum implements NameValueEnum<Integer> {

    OWNER(10, "拥有者"),

    ADMIN(20, "管理员"),

    MEMBER(30, "成员"),

    ;

    private final Integer value;

    private final String name;

    public static final List<EnterpriseMemberRoleEnum> MANAGER_ROLES = List.of(OWNER, ADMIN);

    public static final String SECURITY_ROLE_PREFIX = "ROLE_ENTERPRISE_";
}
