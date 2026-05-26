package io.github.loncra.basic.service.resource.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.basic.service.auth.api.enumerate.ResourceTypeEnum;
import io.github.loncra.basic.service.commons.domain.metadata.ExportDataMetadata;
import io.github.loncra.basic.service.commons.enumerate.ResourceSourceEnum;
import io.github.loncra.basic.service.resource.api.enumerate.AttachmentTypeEnum;
import io.github.loncra.basic.service.resource.server.domain.body.CompleteUploadRequestBody;
import io.github.loncra.basic.service.resource.server.service.AttachmentService;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.enumerate.ValueEnum;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.minio.*;
import io.github.loncra.framework.crypto.algorithm.Base64;
import io.github.loncra.framework.crypto.algorithm.CodecUtils;
import io.github.loncra.framework.minio.MinioAsyncTemplate;
import io.github.loncra.framework.minio.ObjectItem;
import io.github.loncra.framework.security.audit.Auditable;
import io.github.loncra.framework.security.plugin.Plugin;
import io.github.loncra.framework.spring.security.core.audit.OperationDataTrace;
import io.github.loncra.framework.spring.security.core.authentication.token.AuditAuthenticationToken;
import io.github.loncra.framework.spring.web.mvc.SpringMvcUtils;
import io.minio.GetObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * 附件管理
 *
 * @author maurice.chen
 * @since 2022-02-16 01:48:39
 */
@Slf4j
@RestController
@RequestMapping("attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("buckets")
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> buckets() throws Exception {
        return attachmentService.buckets();
    }

    @PostMapping("/find")
    @Plugin(
            name = "文件管理",
            id = "attachment_manager",
            parent = "resource",
            type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
            sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
    )
    @PreAuthorize("hasAuthority('perms[resource_server_attachment:find]')")
    public Object find(
            @RequestParam
            String type,
            String filename,
            @RequestParam(required = false, defaultValue = "false")
            boolean formatObjectWriteResult
    ) throws Exception {
        FileObject fileObject = attachmentService.getFileObject(type, filename);
        List<ObjectItem> items = attachmentService.list(fileObject, null);
        if (formatObjectWriteResult) {
            return MinioAsyncTemplate.convertObjectWriteResult(items, fileObject.getBucketName());
        }
        return items;

    }

    /**
     * 删除文件
     *
     * @param fileObjects 文件对象集合
     * @param appendParam 附加参数
     *
     * @return reset 结果集
     */
    @Auditable("文件管理_删除文件")
    @PutMapping("delete")
    @Plugin(name = "删除文件", parent = "attachment_manager", sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE})
    @PreAuthorize("hasAuthority('perms[resource_server_attachment:delete]')")
    public RestResult<?> delete(
            @RequestBody
            List<FileObject> fileObjects,
            @RequestParam
            Map<String, Object> appendParam
    ) throws Exception {
        List<FileObject> convertList = fileObjects.stream()
                .map(f -> attachmentService.getFileObject(f.getBucketName(), f.getObjectName()))
                .toList();
        List<Map<String, Object>> data = attachmentService.delete(convertList, null, appendParam);
        String message = getDeleteMessage(fileObjects);
        return RestResult.ofSuccess(message, data);
    }

    /**
     * 我的资源
     *
     * @param securityContext spring security 上下文
     * @param filename        文件名称
     *
     * @return 对象集合
     */
    @Plugin(
            name = "我的资源",
            id = "my_resource",
            parent = "resource",
            type = ResourceTypeEnum.RESOURCE_MENU_TYPE,
            sources = {ResourceSourceEnum.CONSOLE_SOURCE_VALUE, ResourceSourceEnum.PERSONAL_SOURCE_VALUE}
    )
    @PostMapping("/my/find")
    @PreAuthorize("isAuthenticated()")
    public Object my(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam(required = false, defaultValue = "user.file")
            String type,
            String filename,
            @RequestParam(required = false, defaultValue = "false")
            boolean formatObjectWriteResult
    ) throws Exception {
        Assert.isTrue(
                AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前 Authentication 非 AuditAuthenticationToken 实例，不支持获取附件信息"
        );
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        FileObject fileObject = attachmentService.getFileObject(type, filename);
        List<ObjectItem> items = attachmentService.list(fileObject, token);
        if (formatObjectWriteResult) {
            return MinioAsyncTemplate.convertObjectWriteResult(items, fileObject.getBucketName());
        }
        return items;

    }

    /**
     * 删除资源
     *
     * @param ids 对象名称
     * @param appendParam 附加参数
     *
     * @return reset 结果集
     */
    @Auditable("我的资源_删除信息")
    @PutMapping("my")
    @Plugin(name = "删除信息", parent = "my_resource")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(
            @RequestBody
            List<String> ids,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            Map<String, Object> appendParam
    ) throws Exception {
        Assert.isTrue(
                AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前 Authentication 非 AuditAuthenticationToken 实例，不支持获取附件信息"
        );
        List<FileObject> files = ids.stream().map(id -> attachmentService.getFileObject(AttachmentTypeEnum.USER_FILE.getValue(), id)).toList();
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        List<Map<String, Object>> data = attachmentService.delete(files, token, appendParam);
        String message = getDeleteMessage(files);
        return RestResult.ofSuccess(message, data);
    }

    private String getDeleteMessage(List<FileObject> fileObjects) {
        String message = "删除 " + fileObjects.size() + " 个文件成功";
        if (fileObjects.size() == 1) {
            message = "删除 " + fileObjects.getFirst().getObjectName() + "文件成功";
        }
        return message;
    }



    /**
     * 查找用户导出数据
     *
     * @param securityContext 安全上下文
     *
     * @return 用户导出数据集合
     */
    @Plugin(
            id = "user_export",
            name = "导出数据",
            type = ResourceTypeEnum.RESOURCE_TOOL_TYPE
    )
    @PostMapping("user/export/find")
    @PreAuthorize("isAuthenticated()")
    public List<ExportDataMetadata> userExport(
            @CurrentSecurityContext
            SecurityContext securityContext
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        return attachmentService.findUserExport(token);
    }

    /**
     * 删除用户导出数据
     *
     * @param securityContext 安全上下文
     * @param ids             导出数据 id 集合
     *
     * @return rest 结果集
     */
    @Auditable("导出数据_删除信息")
    @DeleteMapping("user/export")
    @PreAuthorize("isAuthenticated()")
    @Plugin(name = "删除信息", parent = "user_export")
    public RestResult<Void> deleteUserExport(
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            List<String> ids
    ) {
        AuditAuthenticationToken token = CastUtils.cast(securityContext.getAuthentication());
        attachmentService.deleteUserExport(token, ids);

        return RestResult.of("删除 " + ids.size() + "数据成功");
    }

    /**
     * 完成分片上传
     *
     * @param body        完成分片上传请求体
     * @param appendParam 附加参数
     *
     * @return rest 结果集
     */
    @PreAuthorize("isFullyAuthenticated()")
    @PostMapping("completeMultipartUpload")
    public ObjectWriteResult completeMultipartUpload(
            @RequestBody
            CompleteUploadRequestBody body,
            @RequestParam
            Map<String, Object> appendParam
    ) throws Exception {

        return attachmentService.completeMultipartUpload(body, appendParam);
    }

    /**
     * 创建文件上传
     *
     * @param type        桶类型
     * @param objectName  文件对象
     * @param size        文件大小
     * @param appendParam 附加参数
     *
     * @return rest 结果集
     */
    @PreAuthorize("isFullyAuthenticated()")
    @GetMapping("createMultipartUpload/{type}")
    public RestResult<Map<String, Object>> createMultipartUpload(
            @PathVariable
            String type,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            String objectName,
            @RequestParam(defaultValue = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            String contentType,
            @RequestParam
            Integer size,
            @RequestParam
            Map<String, Object> appendParam
    ) throws Exception {
        FileObject fileObject = attachmentService.getFileObject(type, objectName);
        attachmentService.setExtraHeaders(fileObject, securityContext.getAuthentication());
        Map<String, Object> data = attachmentService.createMultipartUpload(fileObject, contentType, size, appendParam);

        return RestResult.ofSuccess("创建 [" + objectName + "] 的分片信息成功", data);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("uploadPart/{partNumber}/{uploadId}")
    public RestResult<Map<String, Object>> uploadPart(
            @RequestParam("file")
            MultipartFile file,
            @PathVariable
            String uploadId,
            @PathVariable
            int partNumber
    ) throws Exception {

        Map<String, Object> result = attachmentService.uploadPart(file, partNumber, uploadId);
        return RestResult.ofSuccess(result);
    }

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @param type 桶类型
     *
     * @return reset 结果集
     *
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("upload/{type}")
    @PreAuthorize("isFullyAuthenticated()")
    public ObjectWriteResult singleUpload(
            @RequestPart("file")
            MultipartFile file,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @PathVariable
            String type,
            @RequestParam
            Map<String, Object> appendParam
    ) throws Exception {

        AuditAuthenticationToken token = null;
        if (Objects.nonNull(securityContext) && AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass())) {
            token = CastUtils.cast(securityContext.getAuthentication());
        }

        return attachmentService.singleUpload(file, type, token, appendParam);
    }

    /**
     * 查看对象信息
     *
     * @param type     桶类型
     * @param filename 文件名
     *
     * @return 对象信息
     *
     * @throws Exception 获取失败时抛出
     */
    @PreAuthorize("isFullyAuthenticated()")
    @GetMapping("info/{type}/{filename}")
    public Map<String, String> info(
            @PathVariable
            String type,
            @PathVariable
            String filename
    ) throws Exception {

        FileObject fileObject = attachmentService.getFileObject(type, filename);
        GetObjectResponse is = attachmentService.getMinioAsyncTemplate()
                .getObject(fileObject)
                .get();
        Map<String, String> result = new LinkedHashMap<>();
        for (String name : is.headers().names()) {
            result.put(name, is.headers().get(name));
        }

        return result;
    }

    @GetMapping("multiObject")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<byte[]> getMultiObject(
            @RequestParam
            String json,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) throws Exception {
        List<FilenameObject> list = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(json, new TypeReference<>() {}), StringUtils.EMPTY);
        return getMultiObject(list, securityContext);
    }

    @PostMapping("multiObject")
    @PreAuthorize("isFullyAuthenticated()")
    public ResponseEntity<byte[]> getMultiObject(
            @RequestBody
            List<FilenameObject> list,
            @CurrentSecurityContext
            SecurityContext securityContext
    ) throws Exception {

        AuditAuthenticationToken token = null;
        if (AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass())) {
            token = CastUtils.cast(securityContext.getAuthentication());
        }

        if (list.size() == 1) {
            return attachmentService.getObject(list.getFirst(), token, new LinkedHashMap<>());
        }

        ByteArrayOutputStream responseData = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(responseData);

        for (int i = 0; i < list.size(); i++) {
            FilenameObject filenameObject = list.get(i);
            AttachmentTypeEnum attachmentType = ValueEnum.ofEnum(AttachmentTypeEnum.class, filenameObject.getBucketName(), true);
            if (Objects.nonNull(attachmentType)) {
                filenameObject.setBucketName(attachmentService.getAttachmentConfig().getBucketName(attachmentType.getValue()));
            }
            GetObjectResponse is = attachmentService.getMinioAsyncTemplate()
                    .getObject(filenameObject)
                    .get();
            String filename = attachmentService.getObjectResponseFilename(is, filenameObject, filenameObject.getObjectName() + CastUtils.UNDERSCORE + (i + 1));
            ZipEntry zipEntry = new ZipEntry(filename);
            zipOut.putNextEntry(zipEntry);

            byte[] data = IOUtils.toByteArray(is);
            is.close();

            IOUtils.write(data, zipOut);
        }

        IOUtils.close(zipOut, responseData);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(
                SpringMvcUtils.DEFAULT_ATTACHMENT_NAME,
                URLEncoder.encode(attachmentService.getAttachmentConfig().getMultiFileTitle() + CastUtils.DOT + ResourceUtils.URL_PROTOCOL_ZIP, CodecUtils.DEFAULT_ENCODING)
        );

        return new ResponseEntity<>(responseData.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * 获取文件
     *
     * @param type 桶名称
     * @param objectName 文件名称
     *
     * @return 文件流字节
     *
     * @throws Exception 获取失败时抛出
     */
    @GetMapping("{type}")
    public Object get(
            @PathVariable
            String type,
            @RequestParam
            String objectName,
            @RequestParam(required = false, defaultValue = "false")
            boolean base64,
            @CurrentSecurityContext
            SecurityContext securityContext,
            @RequestParam
            Map<String, Object> appendParam
    ) throws Exception {
        AuditAuthenticationToken token = null;
        if (AuditAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass())) {
            token = CastUtils.cast(securityContext.getAuthentication());
        }

        FileObject fileObject = attachmentService.getFileObject(type, objectName);

        ResponseEntity<byte[]> response = attachmentService.getObject(fileObject, token, appendParam);

        if (!base64) {
            return response;
        } else {
            return RestResult.ofSuccess(Base64.encodeToString(Objects.requireNonNull(response.getBody())));
        }
    }

    /**
     * 移动附件
     *
     * @param object 移动文件对象
     *
     * @return rest 结果集
     */
    @PostMapping("move")
    @PreAuthorize("hasRole('FEIGN')")
    public ObjectWriteResult move(
            @RequestBody
            MoveFileObject object
    ) throws Exception {
        return attachmentService.moveObject(object);
    }

    /**
     * 复制附件
     *
     * @param object 复制文件对象
     *
     * @return rest 结果集
     */
    @PostMapping("copy")
    @PreAuthorize("hasRole('FEIGN')")
    public ObjectWriteResult copy(
            @RequestBody
            CopyFileObject object
    ) throws Exception {
        return attachmentService.copyObject(object);
    }
}
