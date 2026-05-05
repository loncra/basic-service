package io.github.loncra.basic.service.auth.server.resolver;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.auth.server.domain.OidcSecurityUserDetailsInfo;
import io.github.loncra.basic.service.auth.server.domain.entity.ResourceEntity;
import io.github.loncra.basic.service.auth.server.domain.entity.merchant.OpenPlatformMerchantClientEntity;
import io.github.loncra.basic.service.auth.server.enumerate.oauth.RegisteredClientScopeEnum;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantService;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CacheProperties;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.id.metadata.TypeIdNameMetadata;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.ScrollPage;
import io.github.loncra.framework.crypto.algorithm.ByteSource;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.oauth2.authentication.oidc.OidcUserInfoAuthenticationResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.util.MultiValueMap;

import java.util.*;

/**
 * 系统用户授权解析器实现
 *
 * @author maurice.chen
 */
public interface SystemUserAuthorizationResolver<T extends AbstractBasicSystemUser> extends OidcUserInfoAuthenticationResolver {

    @Override
    default OidcUserInfo mappingOidcUserInfoClaims(
            OAuth2Authorization oAuth2Authorization,
            Map<String, Object> claims,
            AuditAuthenticationToken token
    ) {
        createOidcUserInfo(oAuth2Authorization, claims, token);
        return new OidcSecurityUserDetailsInfo(claims);
    }

    /**
     * 创建 oidc 用户信息
     *
     * @param oAuth2Authorization oauth2 授权信息
     * @param claims              当前的 claims
     * @param token               认证 token
     */
    void createOidcUserInfo(
            OAuth2Authorization oAuth2Authorization,
            Map<String, Object> claims,
            AuditAuthenticationToken token
    );

    static void createOidcUserInfoClaims(
            OAuth2Authorization oAuth2Authorization,
            Map<String, Object> claims,
            CommonsConfig commonsConfig,
            OpenPlatformMerchantService openPlatformMerchantService,
            AuditAuthenticationToken token,
            ByteSource key
    ) {
        Set<String> scopes = oAuth2Authorization.getAuthorizedScopes();
        if (Map.class.isAssignableFrom(token.getDetails()
                                               .getClass())) {
            return;
        }

        Map<String, Object> details = CastUtils.cast(token.getDetails());
        // 授权邮箱
        String email = Objects.toString(details.get(PrincipalDetailsConstants.EMAIL_KEY), StringUtils.EMPTY);
        if (scopes.contains(RegisteredClientScopeEnum.EMAIL.getValue()) && StringUtils.isNotEmpty(email)) {
            claims.put(StandardClaimNames.EMAIL, commonsConfig.encrypt(email, key.getBase64()));

            YesOrNo verified = CastUtils.cast(details.get(PrincipalDetailsConstants.EMAIL_VERIFIED_KEY));
            if (Objects.nonNull(verified)) {
                claims.put(StandardClaimNames.EMAIL_VERIFIED, YesOrNo.Yes.equals(verified));
            }
        }

        // 授权电话号码
        String phone = Objects.toString(details.get(PrincipalDetailsConstants.PHONE_NUMBER_KEY), StringUtils.EMPTY);
        if (scopes.contains(RegisteredClientScopeEnum.PHONE.getValue()) && StringUtils.isNotEmpty(phone)) {
            claims.put(StandardClaimNames.PHONE_NUMBER, commonsConfig.encrypt(phone, key.getBase64()));

            YesOrNo verified = CastUtils.cast(details.get(PrincipalDetailsConstants.PHONE_NUMBER_VERIFIED_KEY));
            if (Objects.nonNull(verified)) {
                claims.put(StandardClaimNames.PHONE_NUMBER_VERIFIED, YesOrNo.Yes.equals(verified));
            }
        }

        // 授权 openid
        if (scopes.contains(RegisteredClientScopeEnum.OPENID.getValue())) {
            OpenPlatformMerchantClientEntity merchantClient = openPlatformMerchantService.load(oAuth2Authorization.getRegisteredClientId());
            if (Objects.nonNull(merchantClient)) {
                String userUniqueValue = merchantClient.getMerchantId()
                        + CacheProperties.DEFAULT_SEPARATOR
                        + token.getName();
                String cipherText = commonsConfig.encrypt(userUniqueValue);
                claims.put(RegisteredClientScopeEnum.OPENID.getValue(), cipherText);
            }
        }

        // 授权 union_id
        if (scopes.contains(RegisteredClientScopeEnum.UNIONID.getValue())) {
            String cipherText = commonsConfig.encrypt(token.getName());
            claims.put(RegisteredClientScopeEnum.UNIONID.getValue(), cipherText);
        }

        // 授权个人信息
        if (scopes.contains(RegisteredClientScopeEnum.PROFILE.getValue()) && Objects.nonNull(token.getDetails())) {
            Map<String, Object> profile = CastUtils.convertValue(token.getDetails(), CastUtils.MAP_TYPE_REFERENCE);
            claims.put(RegisteredClientScopeEnum.PROFILE.getValue(), profile);
        }

        // 授权角色信息
        if (scopes.contains(RegisteredClientScopeEnum.ROLE.getValue())) {
            claims.put(RegisteredClientScopeEnum.ROLE.getValue(), StringUtils.join(token.getGrantedAuthorities(), CastUtils.COMMA));
        }

        claims.put(TypeIdNameMetadata.TYPE_FIELD_NAME, token.getType());
        if (token.getDetails() instanceof SecurityPrincipal securityPrincipal) {
            claims.put(RestResult.DEFAULT_STATUS_NAME, CastUtils.convertValue(!securityPrincipal.isDisabled(), Map.class));
        }
    }

    /**
     * 通过角色 id 信息获取用户集合
     *
     * @param roleIds 角色 id 集合
     *
     * @return 用户集合
     */
    Collection<T> getByRoleId(List<Long> roleIds);

    /**
     * 加载用户信息
     *
     * @param token 认证 token
     *
     * @return 用户信息
     */
    T load(AuditAuthenticationToken token);

    /**
     * 通过 id 获取用户信息
     *
     * @param id 主键 id
     *
     * @return 用户信息
     */
    T getByIdentity(String id);

    /**
     * 更新密码
     *
     * @param token       当前用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     *
     * @return 更新后的用户信息
     */
    T updatePassword(
            AuditAuthenticationToken token,
            String oldPassword,
            String newPassword
    );

    /**
     * 管理员重置密码
     *
     * @param id 用户 id
     *
     * @return 新密码
     */
    String adminRestPassword(String id);

    /**
     * 根据手机号码创建用户信息
     *
     * @param phoneNumber 手机号码
     *
     * @return 用户信息
     */
    T createByPhoneNumber(String phoneNumber);

    /**
     * 获取用户分页
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     *
     * @return 分页信息
     */
    ScrollPage<T> findPage(
            PageRequest pageRequest,
            MultiValueMap<String, Object> filter
    );

    /**
     * 获取用户类型来源
     *
     * @return 资源来源枚举
     */
    ResourceSourceEnum getSource();

    /**
     * 根据角色权限获取用户集合
     *
     * @param roleAuthority 角色权限
     *
     * @return 用户集合
     */
    List<T> findByRoleAuthority(String roleAuthority);

    /**
     * 更新用户资源
     *
     * @param id        用户 id
     * @param resourceIds 要更新的资源 id 集合
     */
    void updateResources(
            String id,
            List<Long> resourceIds
    );

    /**
     * 获取当前用户资源
     *
     * @param token 认证 token
     * @param list 资源类型
     * @param sourceContains 仅包含的资源来源
     *
     * @return 用户资源
     */
    default List<ResourceEntity> getSystemUserResource(
            AuditAuthenticationToken token,
            List<ResourceTypeEnum> list,
            List<ResourceSourceEnum> sourceContains
    ) {
        return new LinkedList<>();
    }
}
