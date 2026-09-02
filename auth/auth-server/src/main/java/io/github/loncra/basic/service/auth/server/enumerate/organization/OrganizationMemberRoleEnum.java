package io.github.loncra.basic.service.auth.server.enumerate.organization;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 企业成员角色
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrganizationMemberRoleEnum implements NameValueEnum<Integer> {

    OWNER(10, "企业主"),

    ADMIN(20, "管理员"),

    MEMBER(30, "成员"),

    ;

    private final Integer value;

    private final String name;
}
