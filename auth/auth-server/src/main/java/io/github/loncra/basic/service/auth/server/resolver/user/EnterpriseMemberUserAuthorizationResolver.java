package io.github.loncra.basic.service.auth.server.resolver.user;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.enterprise.EnterpriseMemberEntity;
import io.github.loncra.basic.service.auth.server.resolver.SystemUserAuthorizationResolver;
import io.github.loncra.basic.service.auth.server.service.enterprise.EnterpriseMemberService;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantService;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.ScrollPage;
import io.github.loncra.framework.crypto.algorithm.ByteSource;
import io.github.loncra.framework.crypto.algorithm.SimpleByteSource;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class EnterpriseMemberUserAuthorizationResolver implements SystemUserAuthorizationResolver<EnterpriseMemberEntity> {

    private final CommonsConfig commonsConfig;
    private final OpenPlatformMerchantService openPlatformMerchantService;

    private final EnterpriseMemberService enterpriseMemberService;

    @Override
    public void createOidcUserInfo(
            OAuth2Authorization oAuth2Authorization,
            Map<String, Object> claims,
            AuditAuthenticationToken token
    ) {
        String appKey = openPlatformMerchantService.getMerchantAppKeyByClientId(oAuth2Authorization.getRegisteredClientId());
        if (StringUtils.isEmpty(appKey)) {
            return;
        }

        ByteSource byteSource = new SimpleByteSource(appKey);
        SystemUserAuthorizationResolver.createOidcUserInfoClaims(
                oAuth2Authorization,
                claims,
                commonsConfig,
                openPlatformMerchantService,
                token,
                byteSource
        );
    }

    @Override
    public Collection<EnterpriseMemberEntity> findByRoleId(Set<Long> roleIds) {
        return enterpriseMemberService.findByRoleIds(roleIds);
    }

    @Override
    public EnterpriseMemberEntity load(AuditAuthenticationToken token) {
        return getByIdentity(token.getSecurityPrincipal().getId().toString());
    }

    @Override
    public EnterpriseMemberEntity getByIdentity(String id) {
        return enterpriseMemberService.get(id);
    }

    @Override
    public EnterpriseMemberEntity updatePassword(
            AuditAuthenticationToken token,
            String oldPassword,
            String newPassword
    ) {
        throw new UnsupportedOperationException("不支持修改密码");
    }

    @Override
    public void restPassword(
            Long userId,
            String newPassword
    ) {
        throw new UnsupportedOperationException("不支持重置密码");
    }

    @Override
    public List<ResourceEntity> getSystemUserResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> list,
            List<ResourceSourceEnum> sourceContains
    ) {
        return enterpriseMemberService.getResource(token, list, sourceContains);
    }

    @Override
    public String adminRestPassword(String id) {
        throw new UnsupportedOperationException("不支持管理员重置密码");
    }

    @Override
    public EnterpriseMemberEntity createByPhoneNumber(String phoneNumber) {
        // TODO 做到邀请时，在实现。
        return null;
    }

    @Override
    public ScrollPage<EnterpriseMemberEntity> findPage(
            PageRequest pageRequest,
            MultiValueMap<String, Object> filter
    ) {
        return enterpriseMemberService.findPage(pageRequest, filter);
    }

    @Override
    public ResourceSourceEnum getSource() {
        return ResourceSourceEnum.ENTERPRISE;
    }

    @Override
    public List<EnterpriseMemberEntity> findByRoleAuthority(String roleAuthority) {
        throw new UnsupportedOperationException("不支持通过角色权限查询用户");
    }

    @Override
    public void updateResources(
            Long id,
            Set<Long> resourceIds
    ) {
        String json = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(resourceIds));
        enterpriseMemberService.lambdaUpdate()
                .set(EnterpriseMemberEntity::getResourceIds, json)
                .eq(EnterpriseMemberEntity::getId, id)
                .update();
    }

    @Override
    public void updateRole(
            String id,
            Set<Long> roleIds
    ) {
        String json = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(roleIds));
        enterpriseMemberService.lambdaUpdate()
                .set(EnterpriseMemberEntity::getRoleIds, json)
                .eq(EnterpriseMemberEntity::getId, id)
                .update();
    }

    @Override
    public void updateAvatar(
            String id,
            ObjectWriteResult avatar
    ) {
        throw new UnsupportedOperationException("不支持修改头像");
    }

    @Override
    public boolean isSupport(String type) {
        return getSource().getValue().equals(type);
    }
}
