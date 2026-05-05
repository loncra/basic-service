package io.github.loncra.basic.service.auth.server.resolver.user;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.loncra.basic.service.auth.server.domain.AbstractPlatformUser;
import io.github.loncra.basic.service.auth.server.domain.entity.user.PersonalUserEntity;
import io.github.loncra.basic.service.auth.server.resolver.SystemUserAuthorizationResolver;
import io.github.loncra.basic.service.auth.server.service.merchant.OpenPlatformMerchantService;
import io.github.loncra.basic.service.auth.server.service.user.personal.PersonalUserService;
import io.github.loncra.basic.service.commons.config.CommonsConfig;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.ScrollPage;
import io.github.loncra.framework.crypto.algorithm.ByteSource;
import io.github.loncra.framework.crypto.algorithm.SimpleByteSource;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.security.core.entity.AuditAuthenticationSuccessDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 后台用户授权解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class PersonalUserAuthorizationResolver implements SystemUserAuthorizationResolver<PersonalUserEntity> {

    private final CommonsConfig commonsConfig;

    private final PersonalUserService personalUserService;

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
    public Collection<PersonalUserEntity> getByRoleId(List<Long> roleIds) {
        return List.of();
    }

    @Override
    public PersonalUserEntity load(AuditAuthenticationToken token) {
        return getByIdentity(token.getSecurityPrincipal().getId().toString());
    }

    @Override
    public PersonalUserEntity getByIdentity(String id) {
        return personalUserService.get(id);
    }

    @Override
    public PersonalUserEntity updatePassword(
            AuditAuthenticationToken token,
            String oldPassword,
            String newPassword
    ) {

        PersonalUserEntity user = personalUserService.get(token.getSecurityPrincipal().getId().toString());
        PasswordEncoder passwordEncoder = personalUserService.getPasswordEncoder();
        if (!user.getInitialization().getRandomPassword().toBoolean()) {
            Assert.isTrue(passwordEncoder.matches(oldPassword, user.getPassword()), "旧密码错误");
        } else {
            user.getInitialization().setRandomPassword(YesOrNo.No);
            if (token.getDetails() instanceof AuditAuthenticationSuccessDetails details) {
                details.getMetadata().put(PrincipalDetailsConstants.USER_INITIALIZATION_METADATA_KEY, user.getInitialization());
            }
            SecurityContextHolder.getContext().setAuthentication(token);
        }

        personalUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getPassword, passwordEncoder.encode(newPassword))
                .set(PersonalUserEntity::getInitialization, SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(user.getInitialization())))
                .eq(AbstractPlatformUser::getId, user.getId())
                .update();

        return user;
    }

    @Override
    public String adminRestPassword(String id) {
        String password = RandomStringUtils.secure()
                .next(
                        commonsConfig.getAdminRestPasswordLength(),
                        true,
                        true
                );
        String encodePassword = personalUserService.getPasswordEncoder()
                .encode(password);

        personalUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getPassword, encodePassword)
                .eq(PersonalUserEntity::getId, id)
                .update();

        return password;
    }

    @Override
    public PersonalUserEntity createByPhoneNumber(String phoneNumber) {
        throw new UnsupportedOperationException(getSource().getName() + "的用户不支持通过手机号码创建");
    }

    @Override
    public ScrollPage<PersonalUserEntity> findPage(
            PageRequest pageRequest,
            MultiValueMap<String, Object> filter
    ) {
        return personalUserService.findPage(pageRequest, filter);
    }

    @Override
    public ResourceSourceEnum getSource() {
        return ResourceSourceEnum.PERSONAL;
    }

    @Override
    public List<PersonalUserEntity> findByRoleAuthority(String roleAuthority) {
        Wrapper<PersonalUserEntity> wrapper = personalUserService.getQueryGenerator()
                .createQueryWrapperFromMap(Map.of("filter_[roles.*authority_jin]", roleAuthority));
        return personalUserService.find(wrapper);
    }

    @Override
    public void updateResources(
            String id,
            List<Long> resourceIds
    ) {

        String json = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(resourceIds));
        personalUserService.lambdaUpdate()
                .set(AbstractPlatformUser::getResourceIds, json)
                .eq(AbstractPlatformUser::getId, id)
                .update();
    }

    @Override
    public boolean isSupport(String type) {
        return getSource().getValue()
                .equals(type);
    }


}
