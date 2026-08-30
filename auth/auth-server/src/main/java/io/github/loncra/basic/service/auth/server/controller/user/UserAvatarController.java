package io.github.loncra.basic.service.auth.server.controller.user;


import io.github.loncra.basic.service.auth.api.domain.AbstractBasicSystemUser;
import io.github.loncra.basic.service.auth.server.service.AbstractAuthorizationService;
import io.github.loncra.basic.service.commons.config.AttachmentConfig;
import io.github.loncra.basic.service.resource.api.domain.MultipartUploadFile;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("user/avatar")
@RequiredArgsConstructor
public class UserAvatarController {

    private final AttachmentServiceClient attachmentServiceClient;

    private final AttachmentConfig attachmentConfig;

    private final AbstractAuthorizationService<AbstractBasicSystemUser> authorizationService;

    @PostMapping
    @OperationDataTrace
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "修改个人头像", parent = "authority")
    public RestResult<Void> uploadAvatar(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestPart(MultipartUploadFile.DEFAULT_FILE_NAME)
            MultipartFile file
    ) throws IOException {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        MultipartUploadFile uploadFile = new MultipartUploadFile(file);
        ObjectWriteResult objectWriteResult = attachmentServiceClient.singleUploadAttachmentFile(
                uploadFile,
                AttachmentTypeEnum.AVATAR.toString(),
                Map.of(
                        attachmentConfig.getUploadFilePrefixParamName(), token.getName()
                )
        );
        authorizationService.uploadAvatar(token, objectWriteResult);
        return RestResult.of("上传头像成功");
    }

    @PutMapping
    @OperationDataTrace
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "更换个人头像", parent = "authority")
    public RestResult<Void> updateAvatar(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestBody(required = false)
            ObjectWriteResult objectWriteResult
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        if (Objects.nonNull(objectWriteResult)) {
            SystemException.isTrue(AttachmentServiceClient.isInaccessible(token, objectWriteResult.getExtraHeaders()), "该头像不是当前用户可用的头像");
        }
        authorizationService.uploadAvatar(token, objectWriteResult);
        return RestResult.of("更换个人头像");
    }

}
