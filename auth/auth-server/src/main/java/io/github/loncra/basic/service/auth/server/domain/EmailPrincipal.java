package io.github.loncra.basic.service.auth.server.domain;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;

import java.io.Serializable;

/**
 * 邮箱用户明细
 *
 * @author maurice.chen
 */
public interface EmailPrincipal extends Serializable {

    /**
     * 获取电子邮箱
     *
     * @return 电子邮箱
     */
    String getEmail();

    /**
     * 设置电子邮箱
     *
     * @param email 电子邮箱
     */
    void setEmail(String email);

    /**
     * 是否已验证码邮箱
     *
     * @return 是或否枚举
     */
    YesOrNo getEmailVerified();

    /**
     * 设置电子邮箱是否认证
     *
     * @param verified 是或否枚举
     */
    void setEmailVerified(YesOrNo verified);
}
