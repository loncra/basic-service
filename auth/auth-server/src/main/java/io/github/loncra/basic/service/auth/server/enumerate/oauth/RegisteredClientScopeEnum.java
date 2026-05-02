package io.github.loncra.basic.service.auth.server.enumerate.oauth;

import io.github.loncra.framework.commons.enumerate.NameValueEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * oauth2 注册客户端作用域
 *
 * @author maurice.chen
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RegisteredClientScopeEnum implements NameValueEnum<String> {

    /**
     * 个人资料
     */
    PROFILE("个人资料", OidcScopes.PROFILE, true, "您的个人资料信息。"),

    /**
     * open id
     */
    OPENID("open id", OidcScopes.OPENID, true, "创建属于该应用程序的个人唯一识别信息。"),

    /**
     * union id
     */
    UNIONID("union id", "unionid", true, "您在本系统中的唯一识别信息。"),

    /**
     * 电子邮箱
     */
    EMAIL("电子邮箱", OidcScopes.EMAIL, false, "您的电子邮箱信息。"),

    /**
     * 个人地址
     */
    ADDRESS("个人地址", OidcScopes.ADDRESS, false, "您的个人地址资料信息。"),

    /**
     * 联系电话
     */
    PHONE("联系电话", OidcScopes.PHONE, false, "您的联系电话。"),

    /**
     * 用户角色
     */
    ROLE("用户角色", "role", false, "您的账户角色。");

    private final String name;

    private final String value;

    private final boolean isDefault;

    private final String description;


    @Data
    @AllArgsConstructor
    public static class Description implements Serializable {

        @Serial
        private static final long serialVersionUID = 6543998509887161155L;

        private String scope;

        private String description;

    }

    public Description toDescription() {
        return new Description(getValue(), getDescription());
    }

    public Description toDescription(String description) {
        return new Description(getValue(), Objects.toString(description, getDescription()));
    }

}
