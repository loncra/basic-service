package io.github.loncra.basic.service.auth.server.domain.body;

import io.github.loncra.framework.commons.id.metadata.IdValueMetadata;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserResetPasswordRequestBody implements Serializable {

    private String type;
    private Long userId;
    private IdValueMetadata<String, String> metadata = new IdValueMetadata<>();
    private String newPassword;
    private String confirmPassword;
}
