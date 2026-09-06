package io.github.loncra.basic.service.auth.server.resolver.user;

import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.user.ConsoleUserEntity;
import io.github.loncra.basic.service.auth.server.resolver.SystemUserAuthorizationResolver;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantService;
import io.github.loncra.basic.service.auth.server.service.user.console.ConsoleUserService;
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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * 后台用户授权解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class ConsoleUserAuthorizationResolver implements SystemUserAuthorizationResolver<ConsoleUserEntity> {

    private final CommonsConfig commonsConfig;

    private final ConsoleUserService consoleUserService;

    private final OpenPlatformMerchantService openPlatformMerchantService;

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
    public Collection<ConsoleUserEntity> findByRoleId(Set<Long> roleIds) {
        return consoleUserService.findByRoleIds(roleIds);
    }

    @Override
    public ConsoleUserEntity load(AuditAuthenticationToken token) {
        return getByIdentity(token.getSecurityPrincipal().getId().toString());
    }

    @Override
    public ConsoleUserEntity getByIdentity(String id) {
        return consoleUserService.getByIdentity(id);
    }

    @Override
    public ConsoleUserEntity updatePassword(
            AuditAuthenticationToken token,
            String oldPassword,
            String newPassword
    ) {

        ConsoleUserEntity user = consoleUserService.get(token.getSecurityPrincipal().getId().toString());
        PasswordEncoder passwordEncoder = consoleUserService.getPasswordEncoder();
        Assert.isTrue(passwordEncoder.matches(oldPassword, user.getPassword()), "旧密码错误");

        consoleUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getPassword, passwordEncoder.encode(newPassword))
                .eq(AbstractPlatformUser::getId, user.getId())
                .update();

        return user;
    }

    @Override
    public void restPassword(
            Long userId,
            String newPassword
    ) {
        String encodePassword = consoleUserService.getPasswordEncoder()
                .encode(newPassword);
        consoleUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getPassword, encodePassword)
                .eq(AbstractPlatformUser::getId, userId)
                .update();
    }

    @Override
    public String adminRestPassword(String id) {
        String password = RandomStringUtils.secure()
                .next(
                        commonsConfig.getAdminRestPasswordLength(),
                        true,
                        true
                );
        String encodePassword = consoleUserService.getPasswordEncoder()
                .encode(password);

        consoleUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getPassword, encodePassword)
                .eq(ConsoleUserEntity::getId, id)
                .update();

        return password;
    }

    @Override
    public ConsoleUserEntity createByPhoneNumber(String phoneNumber) {
        throw new UnsupportedOperationException(getSource().getName() + "的用户不支持通过手机号码创建");
    }

    @Override
    public ScrollPage<ConsoleUserEntity> findPage(
            PageRequest pageRequest,
            MultiValueMap<String, Object> filter
    ) {
        return consoleUserService.findPage(pageRequest, filter);
    }

    @Override
    public ResourceSourceEnum getSource() {
        return ResourceSourceEnum.CONSOLE;
    }

    @Override
    public List<ConsoleUserEntity> findByRoleAuthority(String roleAuthority) {
        throw new UnsupportedOperationException("不支持通过角色权限查询用户");
    }

    @Override
    public void updateResources(
            Long id,
            Set<Long> resourceIds
    ) {
        String json = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(resourceIds));
        consoleUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getResourceIds, json)
                .eq(AbstractPlatformUser::getId, id)
                .update();
    }


    @Override
    public List<ResourceEntity> getSystemUserResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> list,
            List<ResourceSourceEnum> sourceContains
    ) {
        return consoleUserService.getResource(token, list, sourceContains);
    }

    @Override
    public void updateRole(
            String id,
            Set<Long> roleIds
    ) {
        String json = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(roleIds));
        consoleUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getRoleIds, json)
                .eq(AbstractPlatformUser::getId, id)
                .update();
    }

    @Override
    public void updateAvatar(
            String id,
            ObjectWriteResult avatar
    ) {

        String json = null;
        if (Objects.nonNull(avatar)) {
            json = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(avatar));
        }
        consoleUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getAvatar, json)
                .eq(AbstractPlatformUser::getId, id)
                .update();
    }

    @Override
    public boolean isSupport(String type) {
        return getSource().getValue()
                .equals(type);
    }
}
