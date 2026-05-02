package io.github.loncra.basic.service.auth.server.resolver;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;

/**
 * 自动注册的系统用户登录类型解析器
 *
 * @author mauirce.chen
 */
public interface LoginTypeResolver {

    /**
     * 是否支持登录类型
     *
     * @param loginType 登录类型
     * @return true 是，否则 false
     */
    boolean isSupport(String loginType);

    /**
     * 获取用户登录账户
     *
     * @param token 认证 token 请求
     *
     * @return 登录账户
     */
    String getUsername(RequestAuthenticationToken token);

    /**
     * 匹配登录密码
     *
     * @param presentedPassword 表单提交的密码
     * @param token 认证 token 请求
     * @param principal 当前 spring security 上下文
     *
     * @return 匹配成功返回 true，否则 false
     */
    Boolean matchesPassword(
            String presentedPassword,
            RequestAuthenticationToken token,
            SecurityPrincipal principal
    );

    /**
     * 新增用户钱触发此方法
     *
     * @param token 认证 token 请求
     * @param user 新增用户信息
     */
    <T extends AbstractBasicSystemUser> void preInsertUser(
            RequestAuthenticationToken token,
            T user
    );
}
