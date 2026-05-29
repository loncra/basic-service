package io.github.loncra.basic.service.auth.api.service.web;


import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.api.domain.AbstractWechatAuthentication;
import io.github.loncra.basic.service.auth.api.service.SystemUserServiceClient;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * 系统用户服务客户端
 *
 * @author maurice.chen
 */
@HttpExchange
public interface SystemUserServiceWebClient extends SystemUserServiceClient {


    /**
     * 获取系统用户
     *
     * @param systemName 用户系统名称
     *
     * @return 系统用户
     */
    @Override
    @PostExchange("system/user/{systemName}")
    Map<String, Object> getSystemUser(
            @PathVariable
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
    @Override
    @PostExchange("system/user/find/{type}")
    List<Map<String, Map<String, Object>>> findSystemUser(
            @PathVariable(required = false)
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
    @Override
    @PostExchange("createSystemUserByPhoneNumber")
    <T extends AbstractBasicSystemUser> T createSystemUserByPhoneNumber(
            @RequestParam
            String phoneNumber,
            @RequestParam
            String type
    );

    /**
     * 获取微信认证信息
     *
     * @param principal 用户表达式 (用户类型:用户主键 id)
     *
     * @return 微信认证信息
     */
    @Override
    @GetExchange("getWechatAuthentication")
    <T extends AbstractWechatAuthentication> T getWechatAuthentication(
            @RequestParam
            String principal
    );
}
