package io.github.loncra.basic.service.auth.server.enumerate;

import io.github.loncra.framework.commons.enumerate.NameEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 登录类型
 *
 * @author maurice
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LoginTypeEnum implements NameEnum {

    /**
     * 登录账户与密码登录
     */
    USERNAME_PASSWORD("登录账户与密码登录"),

    /**
     * 手机验证码登录
     */
    PHONE_CAPTCHA("手机验证码登录"),

    /**
     * 一键手机号码登录
     */
    PHONE_AUTHENTICATION("一键手机号码登录"),

    /**
     * 微信登录
     */
    WECHAT_AUTHENTICATION("微信登录"),

    /**
     * 企业微信登录
     */
    WORK_WECHAT_AUTHENTICATION("企业微信登录"),

    ;

    private final String name;

    public static final List<String> LOAD_DATABASE_TYPES = List.of(USERNAME_PASSWORD.toString(), PHONE_CAPTCHA.toString());
}
