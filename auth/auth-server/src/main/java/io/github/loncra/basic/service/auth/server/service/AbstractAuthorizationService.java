package io.github.loncra.basic.service.auth.server.service;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.resolver.SystemUserAuthorizationResolver;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.HttpRequestParameterMapUtils;
import io.github.loncra.framework.commons.exception.ServiceException;
import io.github.loncra.framework.commons.id.metadata.IdNameMetadata;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.ScrollPage;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * 授权服务
 *
 * @param <T>
 *
 * @author maurice.chen
 */
@Getter
@Setter(onMethod_ = @Autowired)
public abstract class AbstractAuthorizationService<T extends AbstractBasicSystemUser> {

    private List<SystemUserAuthorizationResolver<T>> systemUserAuthorizationResolvers;

    private AccessTokenContextRepository accessTokenContextRepository;

    public SystemUserAuthorizationResolver<T> getSystemUserAuthorizationResolver(
            String type,
            boolean notFoundThrowException
    ) {
        Optional<SystemUserAuthorizationResolver<T>> optional = systemUserAuthorizationResolvers
                .stream()
                .filter(r -> r.isSupport(type))
                .findFirst();

        if (optional.isEmpty() && notFoundThrowException) {
            throw new ServiceException("找不到类型为 [" + type + "] 的系统用户解析器实现");
        }

        return optional.orElse(null);
    }


    /*public void syncSystemUserRole(
            List<BasicSystemRole> roles,
            SystemUserRoleSyncResolver systemUserRoleSyncResolver
    ) {
        List<ResourceSourceEnum> sources = roles
                .stream()
                .flatMap(g -> g.getSources().stream())
                .toList();

        Set<Long> groupIds = roles.stream()
                .map(BasicSystemRole::getId)
                .collect(Collectors.toSet());

        for (ResourceSourceEnum source : sources) {
            SystemUserAuthorizationResolver<T> userAuthorizationResolver = getSystemUserAuthorizationResolver(source.getValue(), false);

            List<T> systemUsers = new LinkedList<>(userAuthorizationResolver.getByRoleId(groupIds));
            if (CollectionUtils.isEmpty(systemUsers)) {
                continue;
            }

            systemUsers.forEach(sue -> systemUserRoleSyncResolver.syncSystemUserGroup(sue, roles));
        }
    }*/

    public List<IdNameMetadata> getSystemUserTypes() {
        List<IdNameMetadata> result = new ArrayList<>();
        for (SystemUserAuthorizationResolver<T> resolver : getSystemUserAuthorizationResolvers()) {
            IdNameMetadata metadata = new IdNameMetadata();
            metadata.setId(resolver.getSource().getValue());
            metadata.setName(resolver.getSource().getName());
            result.add(metadata);
        }
        return result;
    }

    public String adminRestPassword(
            String type,
            String id
    ) {
        return getSystemUserAuthorizationResolver(type, true).adminRestPassword(id);
    }

    public T updatePassword(
            AuditAuthenticationToken token,
            String oldPassword,
            String newPassword
    ) {
        return getSystemUserAuthorizationResolver(token.getType(), true)
                .updatePassword(token, oldPassword, newPassword);
    }

    public Map<IdNameMetadata, List<T>> findSystemUser(
            PageRequest pageRequest,
            List<String> ignoreTypes,
            HttpServletRequest request
    ) {
        MultiValueMap<String, String> parameter = HttpRequestParameterMapUtils.castMapToMultiValueMap(request.getParameterMap());
        MultiValueMap<String, Object> filter = new LinkedMultiValueMap<>();
        parameter.forEach(filter::addAll);

        Map<IdNameMetadata, List<T>> result = new LinkedHashMap<>();
        for (SystemUserAuthorizationResolver<T> resolver : getSystemUserAuthorizationResolvers()) {
            if (ignoreTypes.contains(resolver.getSource().getName())) {
                continue;
            }

            MultiValueMap<String, Object> resolverFilter = new LinkedMultiValueMap<>(filter);
            String prefix = resolver.getSource().getName() + CacheProperties.DEFAULT_SEPARATOR;
            List<String> keys = filter
                    .keySet()
                    .stream()
                    .filter(key -> Strings.CS.startsWith(key, prefix))
                    .toList();
            for (String key : keys) {
                List<Object> value = filter.get(key);
                String finalKey = Strings.CS.removeStart(key, prefix);
                resolverFilter.put(finalKey, value);
                resolverFilter.remove(key);
            }

            ScrollPage<T> page = resolver.findPage(pageRequest, resolverFilter);
            if (page.getNumberOfElements() <= 0) {
                continue;
            }

            IdNameMetadata metadata = new IdNameMetadata();
            metadata.setId(resolver.getSource().getValue());
            metadata.setName(resolver.getSource().getName());

            result.put(metadata, page.getElements());
        }

        return result;
    }

    public List<ResourceEntity> getSystemUserResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> list,
            List<ResourceSourceEnum> sourceContains
    ) {
        return getSystemUserAuthorizationResolver(token.getType(), true)
                .getSystemUserResource(
                        token,
                        list,
                        sourceContains
                );
    }

    public void uploadAvatar(
            AuditAuthenticationToken token,
            ObjectWriteResult avatar
    ) {
        AuditAuthenticationSuccessDetails details = Objects.requireNonNull(CastUtils.cast(token.getDetails()));
        if (Objects.nonNull(avatar)) {
            details.getMetadata().put(PrincipalDetailsConstants.AVATAR_KEY, avatar);
        } else {
            details.getMetadata().remove(PrincipalDetailsConstants.AVATAR_KEY);
        }
        accessTokenContextRepository.saveAuthentication(token);
        getSystemUserAuthorizationResolver(token.getType(), true)
                .updateAvatar(
                        token.getSecurityPrincipal().getId().toString(),
                        avatar
                );
    }
}
