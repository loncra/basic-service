package io.github.loncra.basic.service.auth.server.service;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.EmailPrincipal;
import io.github.loncra.basic.service.auth.server.domain.PhoneNumberPrincipal;
import io.github.loncra.basic.service.auth.server.resolver.SystemUserAuthorizationResolver;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.jackson.serializer.DesensitizeSerializer;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.security.entity.support.SimpleSecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.TypeSecurityPrincipalService;
import io.github.loncra.framework.spring.security.core.authentication.service.TypeSecurityPrincipalManager;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.authentication.token.TypeAuthenticationToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 简单的 RBAC 授权服务实现
 *
 * @author maurice.chen
 */
@Getter
@Component
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RedissonCacheAuthorizationService<T extends AbstractBasicSystemUser> extends AbstractAuthorizationService<T> {

    private final RedissonClient redissonClient;

    private final TypeSecurityPrincipalManager typeSecurityPrincipalManager;

    private final AccessTokenContextRepository accessTokenContextRepository;

    /**
     * 删除所有认证缓存
     *
     * @param sources 资源来源枚举
     */
    public void deleteAuthorizationCache(Set<ResourceSourceEnum> sources) {
        List<TypeAuthenticationToken> tokens = sources.stream()
                .map(s -> new TypeAuthenticationToken(DesensitizeSerializer.DEFAULT_DESENSITIZE_SYMBOL, null, s.toString()))
                .toList();

        for (TypeAuthenticationToken token : tokens) {
            CacheProperties cacheProperties = typeSecurityPrincipalManager.getTypeSecurityPrincipalService(token.getType())
                    .getAuthorizationCache(token, null);

            if (Objects.isNull(cacheProperties)) {
                continue;
            }

            String key = cacheProperties.getName();
            redissonClient.getBucket(key).deleteAsync();
        }
    }

    public void deleteSystemUserAllCache(String principal) {
        TypeIdNameMetadata typeIdNameMetadata = TypeIdNameMetadata.ofPrincipalString(principal);
        SystemUserAuthorizationResolver<T> resolver = getSystemUserAuthorizationResolver(typeIdNameMetadata.getType(), false);
        if (Objects.isNull(resolver)) {
            return ;
        }

        T entity = resolver.getByIdentity(typeIdNameMetadata.getId());
        if (Objects.isNull(entity)) {
            return ;
        }

        deleteSystemUserAllCache(entity, ResourceSourceEnum.valueOf(typeIdNameMetadata.getType()));
    }

    public void deleteSystemUserAllCache(
            T entity,
            ResourceSourceEnum resourceSource
    ) {
        List<TypeAuthenticationToken> tokens = new LinkedList<>();

        tokens.add(new TypeAuthenticationToken(entity.getUsername(), null, resourceSource.toString()));
        tokens.add(new TypeAuthenticationToken(entity.getId(), null, resourceSource.toString()));

        if (entity instanceof EmailPrincipal principal && StringUtils.isNotEmpty(principal.getEmail())) {
            tokens.add(new TypeAuthenticationToken(principal.getEmail(), null, resourceSource.toString()));
        }

        if (entity instanceof PhoneNumberPrincipal principal && StringUtils.isNotEmpty(principal.getPhoneNumber())) {
            tokens.add(new TypeAuthenticationToken(principal.getPhoneNumber(), null, resourceSource.toString()));
        }

        List<String> keys = new LinkedList<>();
        TypeSecurityPrincipalService typeSecurityPrincipalService = typeSecurityPrincipalManager.getTypeSecurityPrincipalService(resourceSource.toString());

        tokens.stream()
                .map(typeSecurityPrincipalService::getAuthenticationCache)
                .filter(Objects::nonNull)
                .map(CacheProperties::getName)
                .forEach(keys::add);
        SecurityPrincipal principal = new SimpleSecurityPrincipal(entity.getId(), null, entity.getUsername());
        CacheProperties authorizationCache = typeSecurityPrincipalService.getAuthorizationCache(tokens.getFirst(), principal);
        if (Objects.nonNull(authorizationCache)) {
            String authorizationCacheKey = authorizationCache.getName();
            keys.add(authorizationCacheKey);
        }

        redissonClient.getKeys()
                .delete(keys.toArray(new String[0]));
        getAccessTokenContextRepository()
                .deleteSecurityContext(resourceSource.getValue(), entity.getId().toString());
    }

    @Override
    public String adminRestPassword(
            String type,
            String id
    ) {
        String result = super.adminRestPassword(type, id);
        deleteSystemUserAllCache(type + CacheProperties.DEFAULT_SEPARATOR + id);
        return result;
    }

    @Override
    public T updatePassword(
            AuditAuthenticationToken token,
            String oldPassword,
            String newPassword
    ) {
        T result = super.updatePassword(token, oldPassword, newPassword);
        if (Objects.nonNull(result)) {
            deleteSystemUserAllCache(result.getSystemName());
        }
        return result;
    }
}
