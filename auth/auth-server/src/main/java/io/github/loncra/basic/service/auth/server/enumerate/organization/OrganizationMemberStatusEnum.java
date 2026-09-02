package io.github.loncra.basic.service.auth.server.enumerate.organization;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 企业成员状态
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrganizationMemberStatusEnum implements NameValueEnum<Integer> {

    INVITED(10, "待加入"),

    ACTIVE(20, "已加入"),

    DISABLED(30, "已禁用"),

    ;

    private final Integer value;

    private final String name;
}
