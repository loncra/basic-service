package io.github.loncra.basic.service.resource.server.domain.body.captcha;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.server.service.captcha.SimpleCaptchaMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信验证码请求体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsRequestBody extends SimpleCaptchaMessageType implements Serializable {

    @Serial
    private static final long serialVersionUID = 1235954873943241073L;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION, message = "手机号码格式错误")
    private String phoneNumber;

}
