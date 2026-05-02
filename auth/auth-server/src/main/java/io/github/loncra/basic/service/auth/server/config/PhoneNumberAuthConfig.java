package io.github.loncra.basic.service.auth.server.config;

import io.github.loncra.basic.service.commons.constants.PrincipalDetailsConstants;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 手机号码一键登录配置信息
 *
 * @author maurice.chen
 */
@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("loncra.basic-service.auth.app.phone-number-auth")
public class PhoneNumberAuthConfig {

    public static final String DEFAULT_OUT_ID_PARAM_NAME = "outId";

    private String outIdParamName = DEFAULT_OUT_ID_PARAM_NAME;

    private String tokenParamName = SystemConstants.ACCESS_TOKEN_FIELD_NAME;

    private String phoneNumberParamName = PrincipalDetailsConstants.PHONE_NUMBER_KEY;

    private String typeParamName = "phoneNumberAuthenticationType";

    private String channelParamName = "channel";

    private String smsCaptchaTypeParamName = "captchaType";

    private String smsCaptchaValueParamName = "_smsCaptcha";

    private String smsCaptchaTokenParamName = "_smsCaptchaToken";
}
