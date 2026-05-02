package io.github.loncra.basic.service.resource.server.reslover.attachment;

import io.github.loncra.basic.service.commons.config.AttachmentConfig;
import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.server.reslover.AttachmentResolver;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.ErrorCodeException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.minio.UserMetadataFileObject;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基础文件解析器实现
 *
 * @author maurice.chen
 */
@Component
@RequiredArgsConstructor
public class BasicFileResolver implements AttachmentResolver {

    private final AttachmentConfig attachmentConfig;

    private final AmqpTemplate amqpTemplate;

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isSupport(AttachmentTypeEnum attachmentType) {
        return true;
    }

    @Override
    public RestResult<Map<String, Object>> preUpload(
            MultipartFile file,
            UserMetadataFileObject fileObject,
            AuditAuthenticationToken token,
            Map<String, Object> appendParam
    ) {

        RestResult<Map<String, Object>> result = preValid(fileObject, appendParam);

        if (Objects.nonNull(result)) {
            return result;
        }

        return AttachmentResolver.super.preUpload(file, fileObject, token, appendParam);
    }

    @Override
    public RestResult<Map<String, Object>> preCreateMultipartUpload(
            FileObject fileObject,
            int size,
            Map<String, Object> appendParam
    ) {
        RestResult<Map<String, Object>> result = preValid(fileObject, appendParam);

        if (Objects.nonNull(result)) {
            return result;
        }

        return AttachmentResolver.super.preCreateMultipartUpload(fileObject, size, appendParam);
    }


    public RestResult<Map<String, Object>> preValid(
            FileObject fileObject,
            Map<String, Object> appendParam
    ) {

        String filePrefix = Objects.toString(
                appendParam.get(attachmentConfig.getUploadFilePrefixParamName()),
                StringUtils.EMPTY
        );
        if (StringUtils.isBlank(filePrefix)) {

            List<String> prefixBucketNameList = attachmentConfig
                    .getUploadPrefixType()
                    .stream()
                    .map(attachmentConfig::getBucketName)
                    .toList();

            if (prefixBucketNameList.contains(fileObject.getBucketName()) && attachmentConfig.getUploadPrefixType().contains(fileObject.getBucketName())) {
                return RestResult.of(
                        "参数 " + attachmentConfig.getUploadFilePrefixParamName() + " 不能为空",
                        HttpStatus.BAD_REQUEST.value(),
                        ErrorCodeException.DEFAULT_EXCEPTION_CODE
                );
            }
        }

        if (StringUtils.isNotEmpty(filePrefix)) {
            String path = Strings.CS.appendIfMissing(filePrefix, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            fileObject.setObjectName(path + fileObject.getObjectName());
        }

        return null;
    }

    @Override
    public void postDelete(
            FileObject fileObject,
            Map<String, Object> appendParam
    ) {
        amqpTemplate.convertAndSend(
                SystemConstants.RESOURCE_ATTACHMENT_FANOUT_EXCHANGE,
                StringUtils.EMPTY,
                SystemException.convertSupplier(() -> CastUtils.getObjectMapper().writeValueAsString(fileObject), StringUtils.EMPTY)
        );
    }
}
