package io.github.loncra.basic.service.auth.server.enumerate.enterprise;

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
public enum EnterpriseMemberInvitationEnum implements NameValueEnum<Integer> {

    INVITED(10, "待加入"),

    ACTIVE(20, "已加入"),

    CANCEL(30, "已取消")
    ;

    private final Integer value;

    private final String name;
}
