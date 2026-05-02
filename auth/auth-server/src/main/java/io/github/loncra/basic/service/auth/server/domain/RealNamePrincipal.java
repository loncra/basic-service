package io.github.loncra.basic.service.auth.server.domain;

import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;

import java.io.Serializable;

/**
 * 实名认证用户接口
 *
 * @author maurice.chen
 */
public interface RealNamePrincipal extends Serializable {

    /**
     * 获取真实姓名
     *
     * @return 真实姓名
     */
    String getRealName();

    /**
     * 设置真实姓名
     *
     * @param realName 真实姓名
     */
    void setRealName(String realName);

    /**
     * 获取是否实名认证
     *
     * @return 识或否枚举
     */
    YesOrNo getRealNameAuthentication();

    /**
     * 设置是否实名认证
     *
     * @param realNameAuthentication 是或否枚举
     */
    void setRealNameAuthentication(YesOrNo realNameAuthentication);
}
