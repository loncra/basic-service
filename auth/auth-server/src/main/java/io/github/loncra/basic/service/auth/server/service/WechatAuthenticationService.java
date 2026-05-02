package io.github.loncra.basic.service.auth.server.service;


import io.github.loncra.basic.service.auth.server.dao.WechatAuthenticationDao;
import io.github.loncra.basic.service.auth.server.domain.entity.WechatAuthenticationEntity;
import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.mybatis.plus.service.BasicService;
import io.github.loncra.framework.spring.security.core.authentication.AccessTokenContextRepository;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.wechat.domain.WechatUserDetails;
import io.github.loncra.framework.wechat.domain.metadata.applet.PhoneInfoMetadata;
import io.github.loncra.framework.wechat.domain.metadata.applet.SimpleWechatUserDetailsMetadata;
import io.github.loncra.framework.wechat.service.WechatAppletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * tb_wechat_authentication 的业务逻辑
 *
 * <p>Table: tb_wechat_authentication - 第三方认证信息</p>
 *
 * @author maurice.chen
 * @see WechatAuthenticationEntity
 * @since 2025-05-08 03:39:57
 */
@Getter
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "loncra.framework.wechat", value = "enabled", matchIfMissing = true)
public class WechatAuthenticationService extends BasicService<WechatAuthenticationDao, WechatAuthenticationEntity> {

    private final WechatAppletService wechatAppletService;

    private final AccessTokenContextRepository accessTokenContextRepository;

    public WechatAuthenticationEntity getByPrincipal(String name) {
        return lambdaQuery().eq(WechatAuthenticationEntity::getPrincipal, name)
                .one();
    }

    public PhoneInfoMetadata getPhoneNumber(String phoneNumberCode) {
        return wechatAppletService.getPhoneNumber(phoneNumberCode);
    }

    public WechatUserDetails getWechatUserDetails(String authenticationCode) {
        return wechatAppletService.login(authenticationCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public WechatAuthenticationEntity saveWechatAuthentication(
            String principal,
            WechatUserDetails userDetails,
            PhoneInfoMetadata phoneInfo
    ) {
        WechatAuthenticationEntity wechatAuthentication = getByPrincipal(principal);
        if (Objects.isNull(wechatAuthentication)) {
            wechatAuthentication = CastUtils.of(userDetails, WechatAuthenticationEntity.class);
        }

        wechatAuthentication.setPrincipal(principal);
        if (Objects.nonNull(phoneInfo)) {
            wechatAuthentication.getMetadata()
                    .put(PrincipalDetailsConstants.PHONE_NUMBER_KEY, phoneInfo);
        }

        save(wechatAuthentication);
        return wechatAuthentication;
    }

    @Transactional(rollbackFor = Exception.class)
    public WechatUserDetails syncWechatAuthentication(
            String authenticationCode,
            String phoneNumberCode,
            HttpServletRequest request,
            HttpServletResponse response,
            SecurityContext securityContext
    ) {
        WechatUserDetails wechatUserDetails = getWechatUserDetails(authenticationCode);
        PhoneInfoMetadata phoneInfoMetadata = null;
        if (StringUtils.isNotEmpty(phoneNumberCode)) {
            phoneInfoMetadata = getPhoneNumber(phoneNumberCode);
        }
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        WechatAuthenticationEntity wechatAuthentication = saveWechatAuthentication(
                token.getName(),
                wechatUserDetails, phoneInfoMetadata
        );

        return CastUtils.of(wechatAuthentication, SimpleWechatUserDetailsMetadata.class);
    }


}
