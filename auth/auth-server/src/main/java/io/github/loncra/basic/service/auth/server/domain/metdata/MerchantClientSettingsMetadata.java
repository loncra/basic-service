package io.github.loncra.basic.service.auth.server.domain.metdata;

import io.github.loncra.basic.service.auth.server.enumerate.oauth.MacAlgorithmEnum;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.TokenEndpointAuthenticationSigningAlgorithmTypeEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 商户单点登陆客户端设置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class MerchantClientSettingsMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 5736168817225592493L;

    /**
     * 是否要求客户端使用 Proof Key for Code Exchange (PKCE)
     */
    @NotNull
    private YesOrNo requireProofKey = YesOrNo.No;

    /**
     * 是否要求用户每次授权时进行显式同意确认
     */
    @NotNull
    private YesOrNo requireAuthorizationConsent = YesOrNo.Yes;

    /**
     * 授权同意书过期时间配置（默认180天）
     */
    private TimeProperties authorizationConsentExpirationTime = TimeProperties.ofDay(180);

    /**
     * JWK（JSON Web Key）集合的 URL 地址，用于验证JWT签名
     */
    private String jwkSetUrl;

    /**
     * 令牌端点认证签名算法值（默认使用HS256算法）
     */
    private String tokenEndpointAuthenticationSigningAlgorithmValue = MacAlgorithmEnum.HS256.getName();

    /**
     * 令牌端点认证签名算法类型（默认MAC算法）
     */
    private TokenEndpointAuthenticationSigningAlgorithmTypeEnum tokenEndpointAuthenticationSigningAlgorithmType = TokenEndpointAuthenticationSigningAlgorithmTypeEnum.MAC_ALGORITHM;

    public Map<String, Object> toClientSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();

        settings.put(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY, getRequireProofKey().toBoolean());
        settings.put(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, getRequireAuthorizationConsent().toBoolean());
        settings.put(ConfigurationSettingNames.Client.JWK_SET_URL, getJwkSetUrl());

        if (Objects.nonNull(tokenEndpointAuthenticationSigningAlgorithmType) && StringUtils.isNotEmpty(tokenEndpointAuthenticationSigningAlgorithmValue)) {
            Enum<? extends JwsAlgorithm>[] values = CastUtils.cast(
                    tokenEndpointAuthenticationSigningAlgorithmType.getAlgorithmClass()
                            .getEnumConstants()
            );
            Arrays
                    .stream(values)
                    .map(o -> CastUtils.cast(o, JwsAlgorithm.class))
                    .filter(a -> a.getName()
                            .equals(getTokenEndpointAuthenticationSigningAlgorithmValue()))
                    .findFirst()
                    .ifPresent(ve -> settings.put(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM, ve));

        }

        return settings;
    }

    public static MerchantClientSettingsMetadata ofMap(Map<String, Object> settings) {
        MerchantClientSettingsMetadata merchantClientSettings = new MerchantClientSettingsMetadata();

        Boolean requireProofKey = CastUtils.castIfNotNull(settings.get(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY));
        if (Objects.nonNull(requireProofKey)) {
            merchantClientSettings.setRequireProofKey(YesOrNo.ofBoolean(requireProofKey));
        }

        Boolean requireAuthorizationConsent = CastUtils.castIfNotNull(settings.get(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT));
        if (Objects.nonNull(requireAuthorizationConsent)) {
            merchantClientSettings.setRequireAuthorizationConsent(YesOrNo.ofBoolean(requireAuthorizationConsent));
        }

        Object jwkSetUrl = settings.get(ConfigurationSettingNames.Client.JWK_SET_URL);
        if (Objects.nonNull(jwkSetUrl)) {
            merchantClientSettings.setJwkSetUrl(jwkSetUrl.toString());
        }

        Object tokenEndpointAuthenticationSigningAlgorithm = settings.get(ConfigurationSettingNames.Client.TOKEN_ENDPOINT_AUTHENTICATION_SIGNING_ALGORITHM);
        if (Objects.nonNull(tokenEndpointAuthenticationSigningAlgorithm)) {
            TokenEndpointAuthenticationSigningAlgorithmTypeEnum type = TokenEndpointAuthenticationSigningAlgorithmTypeEnum.ofAlgorithmClass(tokenEndpointAuthenticationSigningAlgorithm.getClass());

            merchantClientSettings.setTokenEndpointAuthenticationSigningAlgorithmType(type);
            JwsAlgorithm jwsAlgorithm = CastUtils.cast(tokenEndpointAuthenticationSigningAlgorithm);
            merchantClientSettings.setTokenEndpointAuthenticationSigningAlgorithmValue(jwsAlgorithm.getName());
        }

        return merchantClientSettings;
    }
}
