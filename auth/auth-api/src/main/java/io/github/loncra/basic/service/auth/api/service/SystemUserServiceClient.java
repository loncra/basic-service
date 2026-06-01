package io.github.loncra.basic.service.auth.api.service;


import io.github.loncra.basic.service.auth.api.domain.AbstractWechatAuthentication;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;

import java.util.List;
import java.util.Map;

/**
 * 系统用户服务客户端
 *
 * @author maurice.chen
 */
public interface SystemUserServiceClient {


    /**
     * 获取系统用户
     *
     * @param systemName 系统名称
     *
     * @return 系统用户
     */
    Map<String, Object> getSystemUser(
            String systemName
    );

    /**
     * 查找系统用户
     *
     * @param type   用户类型，参考 {@link ResourceSourceEnum}
     * @param filter 条件过滤器
     *
     * @return 系统用户集合
     */
    List<Map<String, Object>> findSystemUser(
            String type,
            Map<String, Object> filter
    );

    /**
     * 通过手机号码创建系统用户
     *
     * @param phoneNumber 手机号码
     * @param type        用户类型
     *
     * @return 系统用户
     */
    Map<String, Object> createSystemUserByPhoneNumber(
            String phoneNumber,
            String type
    );

    /**
     * 获取微信认证信息
     *
     * @param principal 用户表达式 (用户类型:用户主键 id)
     *
     * @return 微信认证信息
     */
    <T extends AbstractWechatAuthentication> T getWechatAuthentication(
            String principal
    );
}
