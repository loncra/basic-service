package io.github.loncra.basic.service.auth.server.service.merchant;

import io.github.loncra.basic.service.auth.server.config.AuthAppConfig;
import io.github.loncra.basic.service.auth.server.dao.merchant.OpenPlatformMerchantDao;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantClientEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantEntity;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.TimeProperties;
import io.github.loncra.framework.commons.domain.AccessToken;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.id.IdEntity;
import io.github.loncra.framework.crypto.CipherAlgorithmService;
import io.github.loncra.framework.crypto.algorithm.Base64;
import io.github.loncra.framework.crypto.algorithm.cipher.AesCipherService;
import io.github.loncra.framework.crypto.algorithm.cipher.RsaCipherService;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * tb_open_platform_merchant 的业务逻辑
 *
 * <p>Table: tb_open_platform_merchant - 开放平台商户表</p>
 *
 * @author maurice.chen
 * @see OpenPlatformMerchantEntity
 * @since 2023-09-11 08:57:11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenPlatformMerchantService extends BasicService<OpenPlatformMerchantDao, OpenPlatformMerchantEntity> implements RegisteredClientRepository {

    private static final CipherAlgorithmService CIPHER_ALGORITHM_SERVICE = new CipherAlgorithmService();

    private final AuthAppConfig authAppConfig;

    private final RedissonClient redissonClient;

    private final AmqpTemplate amqpTemplate;

    private final OpenPlatformMerchantClientService openPlatformMerchantClientService;

    private final JwtGenerator jwtGenerator;

    private final CommonsConfig commonsConfig;

    @Override
    public int insert(OpenPlatformMerchantEntity entity) {
        Assert.isTrue(!lambdaQuery().eq(OpenPlatformMerchantEntity::getName, entity.getName()).exists(), "商户 [" + entity.getName() + "] 已存在。");

        AesCipherService cipherService = CIPHER_ALGORITHM_SERVICE.getCipherService(CipherAlgorithmService.AES_ALGORITHM);
        String text = entity.getName() + CacheProperties.DEFAULT_SEPARATOR + System.currentTimeMillis();
        if (StringUtils.isEmpty(entity.getAppId())) {
            String appId = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
            entity.setAppId(appId);
        }
        Key key = cipherService.generateKey(authAppConfig.getMerchantAesKeySize());
        entity.setAppKey(Base64.encodeToString(key.getEncoded()));

        RsaCipherService rsaCipherService = CIPHER_ALGORITHM_SERVICE.getCipherService(CipherAlgorithmService.RSA_ALGORITHM);
        KeyPair keyPair = rsaCipherService.generateKeyPair();
        entity.setPrivateKey(Base64.encodeToString(keyPair.getPrivate().getEncoded()));
        entity.setPublicKey(Base64.encodeToString(keyPair.getPublic().getEncoded()));

        int result = super.insert(entity);

        OpenPlatformMerchantClientEntity clientEntity = OpenPlatformMerchantClientEntity.ofOpenPlatformMerchantEntity(entity);
        clientEntity.setRedirectUris(Set.of(commonsConfig.getHost()));
        openPlatformMerchantClientService.insert(clientEntity);
        return result;
    }

    private RBucket<OpenPlatformMerchantEntity> getRedissonBucket(String appId) {
        return redissonClient.getBucket(authAppConfig.getMerchantCache().getName(appId));
    }

    @Override
    public int save(OpenPlatformMerchantEntity entity) {
        int result = super.save(entity);

        if (result > 0) {
            amqpTemplate.convertAndSend(
                    SystemConstants.RESOURCE_OPEN_PLATFORM_MERCHANT_SAVE_FANOUT_EXCHANGE,
                    StringUtils.EMPTY,
                    SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(entity), StringUtils.EMPTY)
            );
        }

        return result;
    }

    public OpenPlatformMerchantEntity loadMerchant(String appId) {

        RBucket<OpenPlatformMerchantEntity> bucket = getRedissonBucket(appId);
        OpenPlatformMerchantEntity entity = bucket.get();
        if (Objects.isNull(entity)) {

            entity = getByAppId(appId);
            Assert.notNull(entity, "找不到 APP ID 为 [" + appId + "] 的商户信息");

            CacheProperties cacheProperties = authAppConfig.getMerchantCache();
            if (Objects.isNull(cacheProperties)) {
                return entity;
            }

            TimeProperties time = cacheProperties.getExpiresTime();
            if (Objects.nonNull(time)) {
                bucket.setAsync(entity, time.getValue(), time.getUnit());
            }
            else {
                bucket.setAsync(entity);
            }

        }
        return entity;
    }

    public OpenPlatformMerchantEntity getByAppId(String appId) {
        return lambdaQuery().eq(OpenPlatformMerchantEntity::getAppId, appId)
                .one();
    }

    public String getMerchantAppKeyByClientId(String clientId) {
        OpenPlatformMerchantClientEntity entity = openPlatformMerchantClientService.get(clientId);
        if (Objects.isNull(entity)) {
            return null;
        }

        OpenPlatformMerchantEntity merchant = get(entity.getMerchantId());

        return Objects.toString(merchant.getAppKey(), StringUtils.EMPTY);
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        OpenPlatformMerchantClientEntity entity = OpenPlatformMerchantClientEntity.ofRegisteredClient(registeredClient);
        openPlatformMerchantClientService.save(CastUtils.of(entity, OpenPlatformMerchantClientEntity.class));
    }

    @Override
    public RegisteredClient findById(String id) {
        return Objects.requireNonNull(load(id), "找不到 ID 为 [" + id + "] 的商户客户端信息")
                .toRegisteredClient();
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        OpenPlatformMerchantClientEntity entity = openPlatformMerchantClientService.lambdaQuery()
                .eq(OpenPlatformMerchantClientEntity::getClientId, clientId)
                .one();

        Assert.notNull(entity, "找不到 ID 为 [" + clientId + "] 的商户客户端信息");

        Assert.isTrue(YesOrNo.Yes.equals(entity.getEnabled()), "ID 为 [" + clientId + "] 的商户客户端已被禁用");

        return entity.toRegisteredClient();
    }

    public OpenPlatformMerchantClientEntity load(String registeredClientId) {
        return openPlatformMerchantClientService.get(registeredClientId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Collection<? extends Serializable> ids,
            boolean errorThrow,
            boolean useFill
    ) {
        int result = ids.stream().mapToInt(id -> deleteById(id, true))
                .sum();
        if (result != ids.size() && errorThrow) {
            String msg = "[开放平台商户] 删除 id 为 [" + ids + "] 存在 " + (ids.size() - result) + "条记录未受影响";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(
            Collection<OpenPlatformMerchantEntity> entities,
            boolean errorThrow
    ) {
        int result = entities.stream().mapToInt(this::deleteByEntity)
                .sum();
        if (result != entities.size() && errorThrow) {
            String msg = "[开放平台商户] 删除 id 为 [" + entities.stream()
                    .map(IdEntity::getId)
                    .toList() + "] 存在 " + (entities.size() - result) + "条记录未受影响";
            throw new SystemException(msg);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(
            Serializable id,
            boolean useFill
    ) {
        return deleteByEntity(get(id));
    }

    @Override
    public int deleteByEntity(OpenPlatformMerchantEntity entity) {
        openPlatformMerchantClientService.deleteByMerchantId(entity.getId());
        return super.deleteByEntity(entity);
    }

    public AccessToken createAccessToken(String merchantClientId, AuditAuthenticationToken token) {
        RegisteredClient registeredClient = findById(merchantClientId);
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(token)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizationGrantType(AuthorizationGrantType.JWT_BEARER)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN);

        OAuth2Token generatedAccessToken = jwtGenerator.generate(tokenContextBuilder.build());
        if (Objects.isNull(generatedAccessToken)) {
            return null;
        }

        return getAccessToken(generatedAccessToken);
    }

    private AccessToken getAccessToken(OAuth2Token generatedAccessToken) {
        AccessToken accessTokenDetails = new AccessToken();
        accessTokenDetails.setValue(generatedAccessToken.getTokenValue());
        if (Objects.nonNull(generatedAccessToken.getExpiresAt()) && Objects.nonNull(generatedAccessToken.getIssuedAt())) {
            accessTokenDetails.setCreationTime(generatedAccessToken.getIssuedAt());
            long expiresAt = generatedAccessToken.getExpiresAt()
                    .minusMillis(generatedAccessToken.getIssuedAt().toEpochMilli())
                    .toEpochMilli();
            accessTokenDetails.setExpiresTime(TimeProperties.ofMilliseconds(expiresAt));
        }
        return accessTokenDetails;
    }

    public AccessToken createInternalAccessToken(AuditAuthenticationToken token) {
        return createAccessToken(authAppConfig.getAccessTokenOpenPlatformMerchantClientId(), token);
    }
}
