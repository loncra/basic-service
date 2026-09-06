package io.github.loncra.basic.service.auth.server.domain.entity.merchant;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.loncra.basic.service.auth.server.domain.metdata.MerchantClientSettingsMetadata;
import io.github.loncra.basic.service.auth.server.domain.metdata.MerchantTokenSettingsMetadata;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.AuthorizationGrantTypeEnum;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.ClientAuthenticationMethodEnum;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.RegisteredClientScopeEnum;
import io.github.loncra.framework.commons.annotation.JsonCollectionGenericType;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.mybatis.handler.JacksonJsonTypeHandler;
import io.github.loncra.framework.mybatis.plus.baisc.VersionEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.Alias;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>Table: tb_open_platform_merchant_client 开放平台商家 OAuth 2 客户端注册信息- </p>
 *
 * @author maurice.chen
 * @since 2023-11-22 03:21:13
 */
@Data
@NoArgsConstructor
@Alias("merchantRegisteredClient")
@TableName(value = "tb_open_platform_merchant_client", autoResultMap = true)
public class OpenPlatformMerchantClientEntity implements VersionEntity<Integer, String> {

    @Serial
    private static final long serialVersionUID = 3848532235829364438L;

    public static final String DOMAIN_REGX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    public static final String BUILDER_VALIDATE_SCOPES_METHOD_NAME = "validateScopes";

    public static final String BUILDER_CREATE_METHOD_NAME = "create";

    /**
     * 主键 id
     */
    private String id;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version = 0;

    /**
     * 创建时间
     */
    @EqualsAndHashCode.Exclude
    private Instant creationTime;

    /**
     * 商户 id
     */
    @NotNull
    private Long merchantId;

    /**
     * 客户端 id
     */
    @NotNull
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String clientId;

    /**
     * 客户端密钥
     */
    @NotNull
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String clientSecret;

    /**
     * 客户端密钥过期时间
     */
    @NotNull
    private Instant clientSecretExpiresAt = LocalDateTime.now()
            .plusYears(BigDecimal.TEN.intValue())
            .atZone(ZoneId.systemDefault())
            .toInstant();

    /**
     * 客户端名称
     */
    @NotNull
    private String clientName;

    /**
     * 是否启用
     */
    @NotNull
    private YesOrNo enabled;

    /**
     * 授权方法
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(ClientAuthenticationMethodEnum.class)
    private List<ClientAuthenticationMethodEnum> clientAuthenticationMethods = List.of(ClientAuthenticationMethodEnum.CLIENT_SECRET_POST);

    /**
     * 认证类型
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(AuthorizationGrantTypeEnum.class)
    private List<AuthorizationGrantTypeEnum> authorizationGrantTypes = List.of(AuthorizationGrantTypeEnum.AUTHORIZATION_CODE);

    /**
     * 重定向 url
     */
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private Set<String> redirectUris = new HashSet<>();

    /**
     * 授权作用域
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    @JsonCollectionGenericType(RegisteredClientScopeEnum.class)
    private Set<RegisteredClientScopeEnum> scopes = Set.of(RegisteredClientScopeEnum.OPENID, RegisteredClientScopeEnum.UNIONID);

    /**
     * 客户端设置
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private MerchantClientSettingsMetadata clientSettings = new MerchantClientSettingsMetadata();

    /**
     * token 设置
     */
    @NotNull
    @TableField(typeHandler = JacksonJsonTypeHandler.class)
    private MerchantTokenSettingsMetadata tokenSettings = new MerchantTokenSettingsMetadata();

    public RegisteredClient toRegisteredClient() {
        RegisteredClient.Builder builder = RegisteredClient
                .withId(getId())
                .clientName(getClientName())
                .clientId(getClientId())
                .clientIdIssuedAt(getCreationTime())
                .clientSecret(getClientSecret())
                .clientSecretExpiresAt(getClientSecretExpiresAt());

        if (CollectionUtils.isNotEmpty(getScopes())) {
            Set<String> scopes = getScopes().stream()
                    .map(RegisteredClientScopeEnum::getValue)
                    .collect(Collectors.toSet());
            builder.scopes(s -> s.addAll(scopes));
        }

        if (CollectionUtils.isNotEmpty(getRedirectUris())) {
            builder.redirectUris(s -> s.addAll(getRedirectUris()));
        }

        if (CollectionUtils.isNotEmpty(getClientAuthenticationMethods())) {
            Set<ClientAuthenticationMethod> methods = getClientAuthenticationMethods()
                    .stream()
                    .map(ClientAuthenticationMethodEnum::getValue)
                    .collect(Collectors.toSet());
            builder.clientAuthenticationMethods(m -> m.addAll(methods));
        }

        if (CollectionUtils.isNotEmpty(getAuthorizationGrantTypes())) {
            Set<AuthorizationGrantType> types = getAuthorizationGrantTypes()
                    .stream()
                    .map(AuthorizationGrantTypeEnum::getValue)
                    .collect(Collectors.toSet());
            builder.authorizationGrantTypes(t -> t.addAll(types));
        }

        builder.clientSettings(ClientSettings.withSettings(getClientSettings().toClientSettings())
                                       .build());

        builder.tokenSettings(TokenSettings.withSettings(getTokenSettings().toTokenSettings())
                                      .build());
        /*Method validateScopesMethod = Objects.requireNonNull(ReflectionUtils.findMethod(builder.getClass(), BUILDER_VALIDATE_SCOPES_METHOD_NAME));
        ReflectionUtils.invokeMethod(validateScopesMethod, builder);
        Method createMethod = Objects.requireNonNull(ReflectionUtils.findMethod(builder.getClass(), BUILDER_CREATE_METHOD_NAME));
        RegisteredClient registeredClient = CastUtils.cast(Objects.requireNonNull(ReflectionUtils.invokeMethod(createMethod, builder)));

        if (CollectionUtils.isNotEmpty(registeredClient.getRedirectUris())) {
            validRedirectUris(getRedirectUris());
        }*/
        return builder.build();
    }

    /*private static void validRedirectUris(Set<String> redirectUris) {
        List<String> urls = redirectUris.stream()
                .filter(s -> !s.matches(DOMAIN_REGX))
                .collect(Collectors.toList());
        Assert.isTrue(CollectionUtils.isEmpty(urls), "url: " + urls + " 非域名地址");
    }*/

    public static OpenPlatformMerchantClientEntity ofRegisteredClient(RegisteredClient client) {
        OpenPlatformMerchantClientEntity entity = new OpenPlatformMerchantClientEntity();

        entity.setId(client.getId());
        entity.setClientId(client.getClientId());
        entity.setClientSecret(client.getClientSecret());

        if (Objects.nonNull(client.getClientSecretExpiresAt())) {
            entity.setClientSecretExpiresAt(client.getClientSecretExpiresAt());
        }

        if (Objects.nonNull(client.getClientIdIssuedAt())) {
            entity.setCreationTime(client.getClientIdIssuedAt());
        }

        if (CollectionUtils.isNotEmpty(client.getScopes())) {
            Set<RegisteredClientScopeEnum> scopeEnums =client.getScopes()
                    .stream()
                    .map(s -> ValueEnum.ofEnum(RegisteredClientScopeEnum.class, s))
                    .collect(Collectors.toSet());
            entity.setScopes(scopeEnums);
        }

        if (CollectionUtils.isNotEmpty(client.getRedirectUris())) {
            entity.setRedirectUris(client.getRedirectUris());
        }

        if (CollectionUtils.isNotEmpty(client.getClientAuthenticationMethods())) {
            List<ClientAuthenticationMethodEnum> methods = client
                    .getClientAuthenticationMethods()
                    .stream()
                    .map(ClientAuthenticationMethodEnum::ofValue)
                    .collect(Collectors.toList());
            entity.setClientAuthenticationMethods(methods);
        }

        if (CollectionUtils.isNotEmpty(client.getAuthorizationGrantTypes())) {
            List<AuthorizationGrantTypeEnum> types = client
                    .getAuthorizationGrantTypes()
                    .stream()
                    .map(AuthorizationGrantTypeEnum::ofValue)
                    .collect(Collectors.toList());
            entity.setAuthorizationGrantTypes(types);
        }

        if (Objects.nonNull(client.getClientSettings()) && MapUtils.isNotEmpty(client.getClientSettings()
                                                                                       .getSettings())) {
            entity.setClientSettings(MerchantClientSettingsMetadata.ofMap(client.getClientSettings()
                                                                                  .getSettings()));
        }

        if (Objects.nonNull(client.getTokenSettings()) && MapUtils.isNotEmpty(client.getTokenSettings()
                                                                                      .getSettings())) {
            entity.setTokenSettings(MerchantTokenSettingsMetadata.ofMap(client.getTokenSettings()
                                                                                .getSettings()));
        }

        if (StringUtils.isEmpty(entity.getId())) {
            entity.setId(DigestUtils.md5DigestAsHex(client.getClientId().getBytes()));
            entity.setCreationTime(Instant.now());
        }

        return entity;

    }
    public static OpenPlatformMerchantClientEntity ofOpenPlatformMerchantEntity(OpenPlatformMerchantEntity merchant) {
        OpenPlatformMerchantClientEntity entity = new OpenPlatformMerchantClientEntity();
        entity.setId(merchant.getAppId());
        entity.setMerchantId(merchant.getId());
        entity.setClientId(merchant.getAppId());
        entity.setClientName(merchant.getName());
        entity.setCreationTime(merchant.getCreationTime());
        entity.setClientSecret(merchant.getAppKey());
        entity.setEnabled(merchant.getEnabled());

        Set<RegisteredClientScopeEnum> registeredClientScopes = Arrays
                .stream(RegisteredClientScopeEnum.values())
                .filter(RegisteredClientScopeEnum::isDefault)
                .collect(Collectors.toSet());
        entity.setScopes(registeredClientScopes);

        List<AuthorizationGrantTypeEnum> authorizationGrantTypes = Arrays
                .stream(AuthorizationGrantTypeEnum.values())
                .filter(AuthorizationGrantTypeEnum::isDefault)
                .collect(Collectors.toList());
        entity.setAuthorizationGrantTypes(authorizationGrantTypes);

        List<ClientAuthenticationMethodEnum> clientAuthenticationMethods = Arrays
                .stream(ClientAuthenticationMethodEnum.values())
                .filter(ClientAuthenticationMethodEnum::isDefault)
                .collect(Collectors.toList());
        entity.setClientAuthenticationMethods(clientAuthenticationMethods);

        return entity;
    }

}