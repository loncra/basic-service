package io.github.loncra.basic.service.resource.api.service.feign;

import io.github.loncra.basic.service.commons.constants.SystemConstants;
import io.github.loncra.basic.service.resource.api.domain.MultipartUploadFile;
import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.minio.CopyFileObject;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.MoveFileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import io.github.loncra.framework.spring.security.core.authentication.service.feign.FeignAuthenticationConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 附件服务客户端
 *
 * @author maurice.chen
 */
@ConditionalOnClass(FeignClientsConfiguration.class)
@FeignClient(name = SystemConstants.SYS_RESOURCE_NAME, contextId = "attachmentServiceClient", configuration = FeignAuthenticationConfiguration.class)
public interface AttachmentServiceFeignClient extends AttachmentServiceClient {

    /**
     * 上传单个附件
     *
     * @param file         文件内容
     * @param type         桶名称
     * @param requestParam 附加参数
     *
     * @return rest 结果集
     */
    @Override
    @PostMapping(value = "attachment/upload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ObjectWriteResult singleUploadAttachmentFile(
            @RequestPart(MultipartUploadFile.DEFAULT_FILE_NAME)
            MultipartFile file,
            @PathVariable
            String type,
            @RequestParam
            Map<String, String> requestParam
    );

    /**
     * 获取文件
     *
     * @param bucketName 桶名称
     * @param objectName 对象名称
     *
     * @return 字节流
     */
    @Override
    @GetMapping("{bucketName}/{objectName}")
    byte[] getAttachmentFile(
            @PathVariable
            String bucketName,
            @PathVariable
            String objectName
    );

    /**
     * 按类型与对象前缀列举附件
     *
     * @param type 桶类型
     * @param filename 对象前缀 / 文件名
     * @param formatObjectWriteResult true 时返回 {@link ObjectWriteResult}
     *
     * @return 对象列表
     */
    @Override
    @PostMapping("attachment/find")
    List<ObjectWriteResult> findAttachment(
            @RequestParam("type")
            String type,
            @RequestParam("filename")
            String filename,
            @RequestParam("formatObjectWriteResult")
            boolean formatObjectWriteResult
    );

    /**
     * 判断文件是否存在
     *
     * @param bucketName 同名称
     * @param objectName 对象名称
     *
     * @return true 存在，否则 false
     */
    /*@Override
    @GetMapping("attachment/isObjectExist")
    boolean isAttachmentFileExist(
            @RequestParam("bucketName")
            String bucketName,
            @RequestParam("objectName")
            String objectName
    );*/

    /**
     * 删除附件
     *
     * @param fileObjects 文件对象集合
     * @param appendParam 附加参数内容
     *
     * @return rest 结果集
     */
    @Override
    @PutMapping("attachment/delete")
    RestResult<Object> deleteAttachment(
            @RequestBody
            List<FileObject> fileObjects,
            @RequestParam
            Map<String, Object> appendParam
    );


    /**
     * 移动附件
     *
     * @param moveFileObject 移动文件对象
     *
     * @return rest 结果集
     */
    @Override
    @PostMapping("attachment/move")
    ObjectWriteResult moveAttachment(
            @RequestBody
            MoveFileObject moveFileObject
    );

    /**
     * 拷贝附件
     *
     * @param fileObject 移动文件对象
     *
     * @return rest 结果集
     */
    @Override
    @PostMapping("attachment/copy")
    ObjectWriteResult copyAttachment(
            @RequestBody
            CopyFileObject fileObject
    );

}
