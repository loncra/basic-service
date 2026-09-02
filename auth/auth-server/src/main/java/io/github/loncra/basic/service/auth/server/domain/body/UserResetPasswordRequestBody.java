package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserResetPasswordRequestBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 218927370787355847L;

    private String type;
    private Long userId;
    private IdValueMetadata<String, String> metadata = new IdValueMetadata<>();
    private String newPassword;
    private String confirmPassword;
}
