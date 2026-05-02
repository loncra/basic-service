package io.github.loncra.basic.service.auth.server.config;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.id.metadata.IdNameValueMetadata;
import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 全局应用配置
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.auth.app")
public class AuthAppConfig {

    /**
     * 默认账户登录类型参数名称
     */
    public static final String DEFAULT_LOGIN_TYPE_PARAM_NAME = "loginType";

    /**
     * 默认注销用户 api 地址
     */
    public static final String DEFAULT_LOGOUT_URL = "/logout";

    /**
     *
     */
    private List<IdNameValueMetadata<String, List<ResourceSourceEnum>>> autoAssociateAllPermissionsRoleAuthorities = List.of(
            new IdNameValueMetadata<>(
                    new IdValueMetadata<>(
                            ResourceSourceEnum.CONSOLE.getAdminAuthority().getId(),
                            List.of(ResourceSourceEnum.CONSOLE)
                    ),
                    ResourceSourceEnum.CONSOLE.getAdminAuthority().getName()
            )
    );

    /**
     * 允许登录错误次数，当达到峰值时，出现验证码
     */
    private Integer allowableFailureNumber = 3;

    /**
     * 登录错误使用的验证码类型
     */
    private String formLoginFailureCaptchaType = "tianai";

    /**
     * app 登录错误使用的验证码类型
     */
    private String appLoginCaptchaType = "tianai";

    /**
     * 超级管理登录账户
     */
    private String adminUsername = "admin";

    /**
     * 允许登录失败次数的缓存配置
     */
    private CacheProperties allowableFailureNumberCache = CacheProperties.of(
            "loncra:basic-service:auth:app:failure:",
            TimeProperties.of(1800, TimeUnit.SECONDS)
    );

    private TimeProperties accessTokenExpiresTime = TimeProperties.of(7, TimeUnit.DAYS);

    /**
     * 创建企业用户时，如果邮箱或手机号码存在值时候，自动认证。
     */
    private boolean enterpriseUserVerified = false;

    /**
     * 登出连接
     */
    private String logoutUrl = DEFAULT_LOGOUT_URL;

    /**
     * 忽略的插件服务集合
     */
    private List<String> ignorePluginService = List.of(SystemConstants.SYS_GATEWAY_NAME, SystemConstants.SYS_SOCKET_SERVER_NAME, "ai-mcp-server::2.0.0-SNAPSHOT", "gateway::2.0.0-SNAPSHOT");

    /**
     * 商户缓存
     */
    private CacheProperties merchantCache = CacheProperties.of(
            "loncra:basic-service:auth:app:merchant:",
            new TimeProperties(1, TimeUnit.DAYS)
    );

    /**
     * 商户秘钥大小
     */
    private int merchantAesKeySize = 256;

    /**
     * 插件资源缓存
     */
    private CacheProperties pluginResourceCache = CacheProperties.of(
            "loncra:basic-service:auth:app:plugin:resource"
    );

    /**
     * access token 开放平台商家 id
     */
    private Integer accessTokenOpenPlatformMerchantClientId = 1;

    /**
     * 扫描插件路径
     */
    private Map<String, List<String>> scanPluginPackages = new LinkedHashMap<>();
}
