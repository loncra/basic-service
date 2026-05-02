package io.github.loncra.basic.service.auth.server.domain;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;

import java.io.Serializable;

/**
 * 带手机号码的用户信息
 *
 * @author maurice.chen
 */
public interface PhoneNumberPrincipal extends Serializable {

    /**
     * 获取手机号码
     *
     * @return 手机号码
     */
    String getPhoneNumber();

    /**
     * 获取是否已验证码手机号码
     *
     * @return 是或否枚举
     */
    YesOrNo getPhoneNumberVerified();

    /**
     * 设置手机号码
     *
     * @param phoneNumber 手机号码
     */
    void setPhoneNumber(String phoneNumber);

    /**
     * 设置手机号码是否验证
     *
     * @param phoneNumberVerified true 是，否则 false
     */
    void setPhoneNumberVerified(YesOrNo phoneNumberVerified);
}
