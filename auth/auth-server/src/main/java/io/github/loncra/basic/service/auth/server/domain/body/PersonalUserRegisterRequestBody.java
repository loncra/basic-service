package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 个人用户注册请求体
 *
 * @author maurice.chen
 */
@Data
public class PersonalUserRegisterRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 3683635944334296928L;

    @NotBlank
    @Pattern(regexp = SystemConstants.PHONE_NUMBER_REGULAR_EXPRESSION)
    private String phoneNumber;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    private String nickname;
}
