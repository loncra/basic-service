package io.github.loncra.basic.service.auth.server.security.handler;

import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.spring.security.core.authentication.handler.JsonAuthenticationSuccessResponse;
import io.github.loncra.framework.spring.security.oauth2.authentication.config.OAuth2Properties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * oauth 授权成功响应内容
 *
 * @author maurice.chen
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthorizeSuccessResponse implements JsonAuthenticationSuccessResponse {

    public static final String ADDITIONAL_PARAMETERS_FIELD_NAME = "additionalParameters";

    private final OAuth2Properties oAuth2Properties;

    private final RedissonClient redissonClient;

    @Override
    public void setting(
            RestResult<Object> result,
            HttpServletRequest request
    ) {
        Object details = result.getData();

        if (!OAuth2AuthorizationCodeRequestAuthenticationToken.class.isAssignableFrom(details.getClass())) {
            return;
        }

        OAuth2AuthorizationCodeRequestAuthenticationToken token = CastUtils.cast(details);
        if (Objects.isNull(token) || Objects.isNull(token.getAuthorizationCode())) {
            return;
        }

        String codeKey = token.getAuthorizationCode()
                .getTokenValue();
        String md5CodeKey = DigestUtils.md5DigestAsHex(codeKey.getBytes(StandardCharsets.UTF_8));
        String cacheKey = oAuth2Properties.getAuthorizationCache()
                .getName(OAuth2ParameterNames.CODE + CacheProperties.DEFAULT_SEPARATOR + md5CodeKey);
        RBucket<OAuth2Authorization> authorizationCodeBucket = redissonClient.getBucket(cacheKey, new SerializationCodec());

        OAuth2Authorization authorization = authorizationCodeBucket.get();
        if (Objects.isNull(authorization)) {
            return;
        }

        OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
        if (Objects.nonNull(authorizationRequest)) {
            Map<String, Object> requestAdditionalParameters = Collections.unmodifiableMap(authorizationRequest.getAdditionalParameters());
            Field field = Objects.requireNonNull(ReflectionUtils.findField(token.getClass(), ADDITIONAL_PARAMETERS_FIELD_NAME));
            ReflectionUtils.setField(field, token, requestAdditionalParameters);
        }
    }
}
