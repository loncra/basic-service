package io.github.loncra.basic.service.auth.server.domain.metdata;

import io.github.loncra.basic.service.auth.server.enumerate.oauth.AccessTokenFormatEnum;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.SignatureAlgorithmEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 商户 token 设置
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
public class MerchantTokenSettingsMetadata implements Serializable {

    @Serial
    private static final long serialVersionUID = 5564109001424545218L;

    /**
     * 授权码有效期配置（默认5分钟）
     */
    @NotNull
    private TimeProperties authorizationCodeTimeToLive = TimeProperties.of(5L, TimeUnit.MINUTES);

    /**
     * 访问令牌有效期配置（默认1天）
     */
    @NotNull
    private TimeProperties accessTokenTimeToLive = TimeProperties.of(1L, TimeUnit.DAYS);

    /**
     * 访问令牌格式（默认自包含格式，即JWT格式）
     */
    @NotNull
    private AccessTokenFormatEnum accessTokenFormat = AccessTokenFormatEnum.SELF_CONTAINED;

    /**
     * 是否复用刷新令牌（默认允许复用）
     */
    @NotNull
    private YesOrNo reuseRefreshTokens = YesOrNo.Yes;

    /**
     * 刷新令牌有效期配置（默认1天）
     */
    @NotNull
    private TimeProperties refreshTokenTimeToLive = TimeProperties.of(1L, TimeUnit.DAYS);

    /**
     * ID Token的签名算法（默认RS256算法）
     */
    @NotNull
    private SignatureAlgorithm idTokenSignatureAlgorithm = SignatureAlgorithmEnum.RS256.getValue();

    public static MerchantTokenSettingsMetadata ofMap(Map<String, Object> settings) {
        MerchantTokenSettingsMetadata merchantTokenSettings = new MerchantTokenSettingsMetadata();

        Duration authorizationCodeTimeToLiveDuration = CastUtils.castIfNotNull(settings.get(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE));
        if (Objects.nonNull(authorizationCodeTimeToLiveDuration)) {
            merchantTokenSettings.setAuthorizationCodeTimeToLive(TimeProperties.of(authorizationCodeTimeToLiveDuration.getSeconds(), TimeUnit.SECONDS));
        }

        Duration accessTokenTimeToLiveDuration = CastUtils.castIfNotNull(settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE));
        if (Objects.nonNull(accessTokenTimeToLiveDuration)) {
            merchantTokenSettings.setAccessTokenTimeToLive(TimeProperties.of(accessTokenTimeToLiveDuration.getSeconds(), TimeUnit.SECONDS));
        }

        Duration refreshTokenTimeToLive = CastUtils.castIfNotNull(settings.get(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE));
        if (Objects.nonNull(refreshTokenTimeToLive)) {
            merchantTokenSettings.setRefreshTokenTimeToLive(TimeProperties.of(refreshTokenTimeToLive.getSeconds(), TimeUnit.SECONDS));
        }

        Object accessTokenFormat = settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT);
        if (Objects.nonNull(accessTokenFormat) && OAuth2TokenFormat.class.isAssignableFrom(accessTokenFormat.getClass())) {
            OAuth2TokenFormat tokenFormat = CastUtils.cast(accessTokenFormat);
            AccessTokenFormatEnum formatEnum = AccessTokenFormatEnum.ofTokenFormat(tokenFormat);
            merchantTokenSettings.setAccessTokenFormat(formatEnum);
        }

        Object idTokenSignatureAlgorithm = settings.get(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM);
        if (Objects.nonNull(idTokenSignatureAlgorithm) && SignatureAlgorithm.class.isAssignableFrom(idTokenSignatureAlgorithm.getClass())) {
            SignatureAlgorithm signatureAlgorithm = CastUtils.cast(idTokenSignatureAlgorithm);
            merchantTokenSettings.setIdTokenSignatureAlgorithm(signatureAlgorithm);
        }

        return merchantTokenSettings;
    }

    public Map<String, Object> toTokenSettings() {
        Map<String, Object> result = new LinkedHashMap<>();

        Duration authorizationCodeTimeToLiveDuration = authorizationCodeTimeToLive.toDuration();
        result.put(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE, authorizationCodeTimeToLiveDuration);

        Duration accessTokenTimeToLiveDuration = accessTokenTimeToLive.toDuration();
        result.put(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, accessTokenTimeToLiveDuration);

        Duration refreshTokenTimeToLiveDuration = refreshTokenTimeToLive.toDuration();
        result.put(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, refreshTokenTimeToLiveDuration);

        result.put(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS, getReuseRefreshTokens().toBoolean());
        result.put(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT, accessTokenFormat.getFormat());
        result.put(ConfigurationSettingNames.Token.ID_TOKEN_SIGNATURE_ALGORITHM, idTokenSignatureAlgorithm);

        return result;
    }

}
