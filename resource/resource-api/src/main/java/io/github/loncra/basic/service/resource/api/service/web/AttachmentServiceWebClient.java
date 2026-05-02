package io.github.loncra.basic.service.resource.api.service.web;

import io.github.loncra.basic.service.resource.api.service.AttachmentServiceClient;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.minio.CopyFileObject;
import io.github.loncra.framework.commons.minio.FileObject;
import io.github.loncra.framework.commons.minio.MoveFileObject;
import io.github.loncra.framework.commons.minio.ObjectWriteResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

/**
 * 附件服务客户端
 *
 * @author maurice.chen
 */
@HttpExchange("attachment")
public interface AttachmentServiceWebClient extends AttachmentServiceClient {

    /**
     * 上传单个附件
     *
     * @param file         文件内容
     * @param type         桶名称
     * @param requestParam 附加参数（在 multipart 请求下由 HTTP Interface 序列化为 URL 查询参数，与 {@code ServletRequest#getParameterMap()} 对齐）
     *
     * @return rest 结果集
     */
    @Override
    @PostExchange(value = "upload/{type}", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    ObjectWriteResult singleUploadAttachmentFile(
            @RequestPart("file")
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
    @GetExchange("{bucketName}/{objectName}")
    byte[] getAttachmentFile(
            @PathVariable
            String bucketName,
            @PathVariable
            String objectName
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
    @GetExchange("isObjectExist")
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
    @PostExchange("delete")
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
    @PostExchange("move")
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
    @PostExchange("copy")
    ObjectWriteResult copyAttachment(
            @RequestBody
            CopyFileObject fileObject
    );

}
