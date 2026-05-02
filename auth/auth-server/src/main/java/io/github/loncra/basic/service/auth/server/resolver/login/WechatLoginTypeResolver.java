package io.github.loncra.basic.service.auth.server.resolver.login;

import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.domain.PhoneNumberPrincipal;
import io.github.loncra.basic.service.auth.server.enumerate.LoginTypeEnum;
import io.github.loncra.basic.service.auth.server.resolver.LoginTypeResolver;
import io.github.loncra.basic.service.auth.server.service.WechatAuthenticationService;
import io.github.loncra.framework.commons.enumerate.basic.YesOrNo;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.security.entity.SecurityPrincipal;
import io.github.loncra.framework.spring.security.core.authentication.token.RequestAuthenticationToken;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import io.github.loncra.framework.wechat.domain.WechatUserDetails;
import io.github.loncra.framework.wechat.domain.metadata.applet.PhoneInfoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 微信小程序登录类型解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "loncra.framework.wechat", value = "enabled", matchIfMissing = true)
public class WechatLoginTypeResolver implements LoginTypeResolver {

    private final WechatAuthenticationService wechatAuthenticationService;

    @Override
    public boolean isSupport(String loginType) {
        return LoginTypeEnum.WECHAT_AUTHENTICATION.toString().equals(loginType);
    }

    @Override
    public String getUsername(RequestAuthenticationToken token) {
        String phoneNumberCodeName = wechatAuthenticationService.getWechatAppletService()
                .getAppletConfig()
                .getPhoneNumberCodeParamName();
        String phoneNumberCode = token.getParameterMap()
                .getFirst(phoneNumberCodeName);

        PhoneInfoMetadata phoneInfo = wechatAuthenticationService.getWechatAppletService()
                .getPhoneNumber(phoneNumberCode);
        SpringMvcUtils.setRequestAttribute(PhoneInfoMetadata.class.getName(), phoneInfo);

        SystemException.isTrue(Objects.nonNull(phoneInfo), "通过 " + phoneNumberCodeName + " 值获取不到用户手机号码");

        return phoneInfo.getPhoneNumber();
    }

    @Override
    public Boolean matchesPassword(
            String presentedPassword,
            RequestAuthenticationToken token,
            SecurityPrincipal principal
    ) {
        WechatUserDetails wechatUserDetails = wechatAuthenticationService.getWechatUserDetails(token.getPrincipal()
                                                                                                       .toString());
        SpringMvcUtils.setRequestAttribute(WechatUserDetails.class.getName(), wechatUserDetails);
        return Objects.nonNull(wechatUserDetails);
    }

    @Override
    public <T extends AbstractBasicSystemUser> void preInsertUser(
            RequestAuthenticationToken token,
            T user
    ) {
        if (user instanceof PhoneNumberPrincipal phoneNumberPrincipal) {
            PhoneInfoMetadata phoneInfo = SpringMvcUtils.getRequestAttribute(PhoneInfoMetadata.class.getName());
            phoneNumberPrincipal.setPhoneNumber(phoneInfo.getPhoneNumber());
            phoneNumberPrincipal.setPhoneNumberVerified(YesOrNo.Yes);
        }
    }
}
