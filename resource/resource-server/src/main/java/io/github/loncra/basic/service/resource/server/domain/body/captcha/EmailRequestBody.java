package io.github.loncra.basic.service.resource.server.domain.body.captcha;

import io.github.loncra.basic.service.resource.server.service.captcha.SimpleCaptchaMessageType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 电子邮件验证码请求体
 *
 * @author maurice
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmailRequestBody extends SimpleCaptchaMessageType implements Serializable {

    @Serial
    private static final long serialVersionUID = 3429703228723485142L;

    /**
     * 电子邮件
     */
    @Email(message = "电子邮件格式不正确")
    @NotBlank(message = "电子邮件不能为空")
    private String email;

    /**
     * 附加变量
     */
    private Map<String, Object> variables = new LinkedHashMap<>();

    /**
     * 操作区域
     */
    private String operation;

    /**
     * 是否替换原始变量名称 true 是，否则 false
     */
    private boolean replaceOriginalVariables;
}
